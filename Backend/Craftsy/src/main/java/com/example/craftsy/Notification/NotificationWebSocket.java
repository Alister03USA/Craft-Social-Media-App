package com.example.craftsy.Notification;

import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/notifications/{username}")
public class NotificationWebSocket {

    private static NotificationRepository notificationRepository;
    private static UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocket.class);

    // Maps to track sessions
    private static final Map<Session, String> sessionUsernameMap = new ConcurrentHashMap<>();
    private static final Map<String, Session> usernameSessionMap = new ConcurrentHashMap<>();


    private static ObjectMapper mapper = new ObjectMapper();

    // Inject repositories via setter so they populate the static fields
    @Autowired
    public void setNotificationRepository(NotificationRepository repo) {
        NotificationWebSocket.notificationRepository = repo;
    }

    @Autowired
    public void setUserRepository(UserRepository repo) {
        NotificationWebSocket.userRepository = repo;
    }

    // Call when a new websocket connection is opened
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "username") String username) {
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);

        logger.info("Connected: " + username);

        try {
            // Fetch the Users object
            Users user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            // Send all unread notifications to the user
            List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

            sendNotificationsToUser(username, unreadNotifications);
        } catch (Exception e) {
            logger.error("Error in onOpen for user: " + username, e);
        }
    }

    // Send a list of notifications to a particular user
    private void sendNotificationsToUser(String username, List<Notification> notifications) {
        Session session = usernameSessionMap.get(username);
        if (session != null && notifications != null) {
            try {
                for (Notification notification : notifications) {
                    String json = mapper.writeValueAsString(notification);
                    session.getBasicRemote().sendText(json);
                }
            } catch (IOException e) {
                logger.error("Error sending notifications to user: " + username, e);
            }
        }
    }

    // Called when a websocket is closed
    @OnClose
    public void onClose(Session session) {
        String username = sessionUsernameMap.get(session);
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);
        logger.info("Disconnected: " + username);
    }

    // Called on websocket error
    @OnError
    public void onError(Session session, Throwable error) {
        String username = sessionUsernameMap.get(session);
        logger.error("WebSocket error for user: " + username, error);
    }

    /** Static method to push a new notification immediately */
    public static void pushNotification(String username, Notification notification) {
        Session session = usernameSessionMap.get(username);
        if (session != null) {
            try {
                String json = mapper.writeValueAsString(notification);
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                logger.error("Error pushing notification to user: " + username, e);
            }
        }
    }
}