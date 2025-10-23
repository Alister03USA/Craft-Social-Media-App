package com.example.craftsy.Notification;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Controller
@ServerEndpoint("ws/notifications/{username}")
public class NotificationWebSocket {

    private static NotificationRepository notificationRepository;

    private static UserRepository userRepository;

    @Autowired
    public void setNotificationRepository(NotificationRepository notificationRepository) {
        notificationRepository = notificationRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    // Maps to track session
    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();

    private static ObjectMapper mapper = new ObjectMapper();

    // Call when a new websocket connection is opened
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "username") String username) {
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);

        System.out.println("Connected: " + username);

        // Fetch the Users object
        Users user =  userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Send all unread notifications to the user
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

        sendNotificationsToUser(username,  unreadNotifications);

    }

    // Send a list of notifications to a particular user
    private void sendNotificationsToUser(String username, List<Notification> notifications) {
        Session session = usernameSessionMap.get(username);
        if (session != null && notifications!= null) {
            try{
                for(Notification notification : notifications) {
                    String json = mapper.writeValueAsString(notification);
                    // Send the unread notifications one by one as json
                    session.getBasicRemote().sendText(json);
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    // Called when a websocket is closed
    @OnClose
    public void onClose(Session session) {
        String username = sessionUsernameMap.get(session);
        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);
        System.out.println("Disconnected: " + username);
    }


    // Called on websocket error
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /** Static method to push a new notification immediately */
    public static void pushNotification(String username, Notification notification) {
        Session session = usernameSessionMap.get(username);
        if (session != null) {
            try {
                String json = mapper.writeValueAsString(notification);
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
