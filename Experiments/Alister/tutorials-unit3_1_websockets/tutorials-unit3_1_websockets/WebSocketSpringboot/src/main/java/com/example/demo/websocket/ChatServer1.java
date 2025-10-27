package com.example.demo.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ServerEndpoint("/chat/1/{username}")
@Component
public class ChatServer1 {

    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(ChatServer1.class);

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {
        logger.info("[onOpen] " + username);

        if (usernameSessionMap.containsKey(username)) {
            session.getBasicRemote().sendText("Username already exists");
            session.close();
        } else {
            sessionUsernameMap.put(session, username);
            usernameSessionMap.put(username, session);

            sendMessageToParticularUser(username, "Welcome to Room 1, " + username);
            broadcast("User: " + username + " has joined Room 1");
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[onMessage] " + username + ": " + message);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Direct messages
        if (message.startsWith("@")) {
            String[] split_msg = message.split("\\s+");
            String destUser = split_msg[0].substring(1);

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < split_msg.length; i++) sb.append(split_msg[i]).append(" ");
            String actualMessage = sb.toString().trim();

            sendMessageToParticularUser(destUser, "[" + timestamp + "][DM from " + username + "]: " + actualMessage);
            sendMessageToParticularUser(username, "[" + timestamp + "][DM to " + destUser + "]: " + actualMessage);
        }
        // Reactions
        else if (message.startsWith("/react")) {
            String[] parts = message.split("\\s+", 3);

            if (parts.length == 2) {
                // Broadcast reaction: /react 👍
                String emoji = parts[1];
                broadcast("[" + timestamp + "] " + username + " reacted " + emoji);
            } else if (parts.length == 3 && parts[1].startsWith("@")) {
                // DM reaction: /react @Alice 👍
                String destUser = parts[1].substring(1);
                String emoji = parts[2];

                sendMessageToParticularUser(destUser, "[" + timestamp + "][DM reaction from " + username + "]: " + emoji);
                sendMessageToParticularUser(username, "[" + timestamp + "][DM reaction to " + destUser + "]: " + emoji);
            }
        }
        // Normal broadcast
        else {
            broadcast("[" + timestamp + "] " + username + ": " + message);
        }
    }



    @OnClose
    public void onClose(Session session) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[onClose] " + username);

        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);

        broadcast(username + " disconnected from Room 1");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String username = sessionUsernameMap.get(session);
        logger.info("[onError] " + username + ": " + throwable.getMessage());
    }

    private void sendMessageToParticularUser(String username, String message) {
        Session userSession = usernameSessionMap.get(username);
        if (userSession != null && userSession.isOpen()) {
            try {
                userSession.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.info("[DM Exception] " + e.getMessage());
            }
        }
    }

    private void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.info("[Broadcast Exception] " + e.getMessage());
            }
        });
    }
}
