package com.example.craftsy;

import com.example.craftsy.Group.GroupMessageWebsocket;
import com.example.craftsy.Group.Repository.GroupMessageRepository;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@Profile("!test")
public class WebSocketConfig {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMessageRepository groupMessageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @PostConstruct
    public void initWebSocketDependencies() {

        // passing Spring-managed beans into WebSocket-managed classes via static setters, so they can use repositories.
        // Initialize GroupMessageWebsocket
        GroupMessageWebsocket.setDependencies(
                groupRepository,
                userRepository,
                groupMessageRepository,
                notificationRepository
        );

        // Initialize NotificationWebSocket
        NotificationWebSocket.setDependencies(
                notificationRepository,
                userRepository
        );

    }
}