package com.example.craftsy.messages.websocket;

import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.messages.Message;
import com.example.craftsy.messages.MessageRepository;
import com.example.craftsy.messages.conversations.*;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Controller
@ServerEndpoint(value = "/chat/{convoId}/{username}", configurator = SpringConfigurator.class)
public class ChatSocket {
    private static final Logger logger = LoggerFactory.getLogger(ChatSocket.class);

    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();
    private static Map<Session, Conversation> sessionConvoMap = new Hashtable<>();
    private static Map<Session, String> sessionConvoTypeMap = new Hashtable<>();

    private MessageService messageService;
    private UserRepository userRepo;
    private MessageRepository msgRepo;
    private GroupConversationRepository groupConvoRepo;
    private DirectConversationRepository directConvoRepo;
    private NotificationRepository notificationRepository;

    public ChatSocket() {}

    public void setGroupConvoRepo(GroupConversationRepository repo) { this.groupConvoRepo = repo; }
    public void setDirectConvoRepo(DirectConversationRepository repo) { this.directConvoRepo = repo; }
    public void setMsgRepo(MessageRepository repo) { this.msgRepo = repo; }
    public void setUserRepo(UserRepository repo) { this.userRepo = repo; }
    public void setMessageService(MessageService service) { this.messageService = service; }
    public void setNotificationRepository(NotificationRepository repo) { this.notificationRepository = repo; }  // ✅ Add setter

    @OnOpen
    public void onOpen(Session session, @PathParam("convoId") String convoId, @PathParam("username") String username) {
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);

        Conversation convo;
        if (convoId.startsWith("D-")) {
            convo = directConvoRepo.findByIdWithMembers(convoId)
                    .orElseThrow(() -> new RuntimeException("Direct conversation not found"));
        } else if (convoId.startsWith("G-")) {
            convo = groupConvoRepo.findByIdWithMembers(convoId)
                    .orElseThrow(() -> new RuntimeException("Group conversation not found"));
        } else {
            throw new RuntimeException("Invalid conversation ID format");
        }

        sessionConvoMap.put(session, convo);
        sessionConvoTypeMap.put(session, convoId.startsWith("G-") ? "GROUP" : "DIRECT");

        boolean inConversation = convo.getMembers()
                .stream()
                .anyMatch(member -> member.getUsername().equals(username));

        if (!inConversation) {
            throw new RuntimeException("User not in conversation");
        }

        logger.info("[ChatSocket] " + username + " connected to conversation " + convoId);
    }

    @OnMessage
    public void onMessage(Session session, String text) throws IOException {
        logger.info("[ChatSocket] Got Message: " + text);

        String senderUsername = sessionUsernameMap.get(session);
        Conversation convo = sessionConvoMap.get(session);
        String convoType = sessionConvoTypeMap.get(session);
        String convoId = convo.getId();

        // Handle reactions
        if (text.startsWith("#react:")) {
            broadcast(messageService.react(text));
            return;
        }

        // Handle replies
        if (text.startsWith("#reply:")) {
            broadcast(messageService.reply(text, senderUsername, convoId));
            sendNotificationsToOthers(convo, senderUsername, "replied to a message", convoType);
            return;
        }

        // Handle remove reaction
        if (text.startsWith("#!react:")) {
            broadcast(messageService.removeReaction(text));
            return;
        }

        // Standard message
        Message message = new Message(senderUsername, text, convo);
        messageService.saveMessageAndUpdateConversation(convo.getId(), message);

        broadcast(senderUsername + ": " + text);

        sendNotificationsToOthers(convo, senderUsername, "sent a message", convoType);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("[ChatSocket] Entered into Close");

        String username = sessionUsernameMap.get(session);
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);
        sessionConvoMap.remove(session);
        sessionConvoTypeMap.remove(session);

        logger.info("[ChatSocket] " + username + " disconnected");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("[ChatSocket] WebSocket error for session {}: {}", session.getId(), throwable.getMessage(), throwable);
        throwable.printStackTrace();
    }


    private void sendNotificationsToOthers(Conversation convo, String senderUsername, String action, String convoType) {
        try {
            // Get sender user object
            Users sender = userRepo.findByUsername(senderUsername).orElse(null);
            if (sender == null) {
                logger.error("[ChatSocket] Sender not found: " + senderUsername);
                return;
            }

            String convoTypeText = convoType.equals("GROUP") ? "group chat" : "direct message";

            // Send notification to all members EXCEPT sender
            convo.getMembers().stream()
                    .filter(member -> !member.getId().equals(sender.getId()))
                    .forEach(member -> {
                        try {
                            // Create notification
                            Notification notif = new Notification();
                            notif.setUser(member);
                            notif.setTitle("New Message");
                            notif.setMessage(senderUsername + " " + action + " in " + convoTypeText);
                            notif.setCreatedAt(new Date());
                            notif.setIsRead(false);

                            // Save to database
                            notificationRepository.save(notif);

                            // Push real-time notification via NotificationWebSocket
                            NotificationWebSocket.pushNotification(member.getUsername(), notif);

                            logger.info("[ChatSocket] Notification sent to " + member.getUsername());
                        } catch (Exception e) {
                            logger.error("[ChatSocket] Error sending notification to " + member.getUsername(), e);
                        }
                    });
        } catch (Exception e) {
            logger.error("[ChatSocket] Error in sendNotificationsToOthers", e);
        }
    }

    private void sendMessageToParticularUser(String username, String message) {
        try {
            usernameSessionMap.get(username).getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("[ChatSocket] Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.error("[ChatSocket] Exception: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String getChatHistory() {
        List<Message> messages = msgRepo.findAll();

        StringBuilder sb = new StringBuilder();
        if (messages != null && messages.size() != 0) {
            for (Message message : messages) {
                sb.append(message.getSender() + ": " + message.getText() + "\n");
            }
        }
        return sb.toString();
    }
}