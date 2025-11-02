package com.example.craftsy.Group;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Entity.GroupMessage;
import com.example.craftsy.Group.Repository.GroupMessageRepository;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/groupMessage/{groupId}/{username}")
public class GroupMessageWebsocket {

    private static Map<Session, String> sessionUsernameMap = new ConcurrentHashMap<>();
    private static Map<String, Session> usernameSessionMap = new ConcurrentHashMap<>();

    // Maps group ID to session - track users in each group
    private static Map<Long, Set<Session>> groupSessionsMap = new ConcurrentHashMap<>();


    private final Logger logger = LoggerFactory.getLogger(GroupMessageWebsocket.class);

    private static GroupRepository groupRepository;
    private static UserRepository userRepository;
    private static GroupMessageRepository groupMessageRepository;
    private static NotificationRepository notificationRepository;

    // Inject Spring beans manually because @ServerEndpoint is not managed by Spring
    public static void setDependencies(GroupRepository gr, UserRepository ur,
                                       GroupMessageRepository gmr, NotificationRepository nr) {
        groupRepository = gr;
        userRepository = ur;
        groupMessageRepository = gmr;
        notificationRepository = nr;
    }


    @OnOpen
    public void onOpen(Session session, @PathParam("groupId") Long groupId, @PathParam("username") String username) {

        // Verify user exists
        Users user = userRepository.findByUsername(username).orElse(null);
        Group group = groupRepository.findById(groupId).orElse(null);

        if (user == null) {
            logger.error("[onOpen] User not found: " + username);
            closeSession(session, "User not found");
            return;
        }

        if (group == null) {
            logger.error("[onOpen] Group not found: " + groupId);
            closeSession(session, "Group not found");
            return;
        }

        // Make sure user is a member of the group
        boolean isMember = group.getMembers().stream().anyMatch(member -> member.getId().equals(user.getId()));

        if(!isMember){
            logger.warn("[onOpen] User is not a member of group: " + groupId);
            closeSession(session, "You are not a member of group: " + groupId);
            return;
        }

        // User is authorized - register the connection
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);
        groupSessionsMap.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).add(session);

        logger.info("onOpen" + username + " successfully joined group " +  groupId );
    ;

        groupSessionsMap.computeIfAbsent(groupId, k-> ConcurrentHashMap.newKeySet()).add(session);

        broadcastToGroup(groupId, "System", username + " has joined the group.");
    }




    @OnMessage
    public void onMessage(Session session, @PathParam("groupId") Long groupId,
                          @PathParam("username") String username, String message) {

        logger.info("[onMessage] " + username + " in group " + groupId + " received message: " + message);

        Users sender = userRepository.findByUsername(username).orElse(null);
        Group group = groupRepository.findById(groupId).orElse(null);

        if (sender == null || group == null) {
            logger.error("[onMessage] Sender or Group not found");
            return;
        }

        // Parse message format: "REPLY:messageId:actualMessage" or just "actualMessage"
        Long replyToMessageId = null;
        String actualMessage = message;

        if (message.startsWith("REPLY:")) {
            String[] parts = message.split(":", 3);
            if (parts.length == 3) {
                try {
                    replyToMessageId = Long.parseLong(parts[1]);
                    actualMessage = parts[2];
                } catch (NumberFormatException e) {
                    logger.error("Invalid reply format: " + message);
                }
            }
        }

        // Save message in groupMessage table
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setSender(sender);
        groupMessage.setGroup(group);
        groupMessage.setMessage(actualMessage);
        groupMessage.setCreatedAt(new Date());

        // Handle reply
        if (replyToMessageId != null) {
            GroupMessage replyToMsg = groupMessageRepository.findById(replyToMessageId).orElse(null);
            if (replyToMsg != null) {
                groupMessage.setReplyToMessage(replyToMsg);
                groupMessage.setReplyToUsername(replyToMsg.getSender().getUsername());
            }
        }

        groupMessageRepository.save(groupMessage);

        // Broadcast message with reply info
        String broadcastMessage = formatBroadcastMessage(username, actualMessage, groupMessage.getReplyToUsername());
        broadcastToGroup(groupId, username, broadcastMessage);

        // Create notifications for all members except sender
        createNotificationsForMembers(group, sender, "sent a message", groupMessage.getId());
    }

    private String formatBroadcastMessage(String sender, String message, String replyToUsername) {
        if (replyToUsername != null) {
            return message + " [replying to @" + replyToUsername + "]";
        }
        return message;
    }



    @OnError
    public void onError(Session session, Throwable error) {
        String username = sessionUsernameMap.get(session);
        logger.error("[On Error]" + username + ": " + error.getMessage());
        error.printStackTrace();

    }



    @OnClose
    public void onClose(Session session, @PathParam("groupId") Long  groupId) {
        String username = sessionUsernameMap.get(session);

        logger.info("[onClose] " + username + " disconnected from group " + groupId);

        sessionUsernameMap.remove(session);

        if (username != null) {
            usernameSessionMap.remove(username);
        }

        Set<Session> groupSessions = groupSessionsMap.get(groupId);
        if (groupSessions != null) {
            groupSessions.remove(session);
            // Clean up empty group session sets
            if (groupSessions.isEmpty()) {
                groupSessionsMap.remove(groupId);
            }
        }

        broadcastToGroup(groupId, "System", username + " left the group.");
    }


    /**
     * Broadcast message to all active sessions in a group
     * @param groupId
     * @param sender
     * @param message
     */
    private void broadcastToGroup(Long groupId, String sender, String message) {
        Set<Session> sessions = groupSessionsMap.get(groupId);
        if (sessions != null) {
            String formattedMsg = "[" + sender + "]: " + message;
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(formattedMsg);
                    } catch (Exception e) {
                        logger.error("[Broadcast Exception] " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Static method to allow external broadcasts (called from REST controller after image upload)
     */
    public static void broadcastToWebSocket(Long groupId, String sender, String message) {
        Set<Session> sessions = groupSessionsMap.get(groupId);
        if (sessions != null) {
            String formattedMessage = "[" + sender + "]: " + message;
            sessions.forEach(s -> {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(formattedMessage);
                    } catch (Exception e) {
                        System.err.println("[Static Broadcast Exception] " + e.getMessage());
                    }
                }
            });
        }
    }



    private void createNotificationsForMembers(Group group, Users sender, String action, Long referenceID) {
        group.getMembers().stream()
                .filter(member -> !member.getUsername().equals(sender.getUsername()))
                .forEach(member -> {
            Notification notification = new Notification();
            notification.setUser(member);
            notification.setTitle("New Group Message");
            notification.setMessage(sender.getUsername() + " " + action + " in " + group.getGroupName());
            notification.setCreatedAt(new Date());
            notification.setIsRead(false);
            notification.setReferenceId(referenceID);
            notificationRepository.save(notification);

            NotificationWebSocket.pushNotification(member.getUsername(), notification);
        });
    }




    // Close the connection due to valid reasons
    private void closeSession(Session session,  String reason) {
        try{
            session.getBasicRemote().sendText("ERROR: " + reason);
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, reason));

        }catch(Exception e){
            logger.error("[closeMessage] Error closing session " + e.getMessage());
        }
    }
}
