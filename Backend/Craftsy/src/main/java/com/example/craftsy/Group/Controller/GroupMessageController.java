package com.example.craftsy.Group.Controller;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Entity.GroupMessage;
import com.example.craftsy.Group.Repository.GroupMessageReadStatusRepository;
import com.example.craftsy.Group.Repository.GroupMessageRepository;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.Group.GroupMessageWebsocket;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.example.craftsy.Group.GroupMessageWebsocket.broadcastToWebSocket;

@RestController
@RequestMapping("/groupMessage")
public class GroupMessageController {

    private static final String UPLOAD_DIR = "uploads/group_images";

    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMessageRepository groupMessageRepository;
    @Autowired private GroupMessageReadStatusRepository groupMessageReadStatusRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;


    /**
     * Get message history for a group
     */
    @GetMapping("/{username}/{groupId}/history")
    public ResponseEntity<?> getMessageHistory(
            @PathVariable Long groupId,
            @PathVariable String username
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Authorization check
        if (!isUserInGroup(user, group)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not a member of this group"));
        }

        List<GroupMessage> messages = groupMessageRepository.findByGroupOrderByCreatedAtAsc(group);
        return ResponseEntity.ok(messages);
    }

    /**
     * Upload an image/media to group
     * NOTE: Text messages should be sent via WebSocket, not REST API
     */
    @PostMapping(
            value = "/{groupId}/{senderUsername}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadMediaMessage(
            @PathVariable Long groupId,
            @PathVariable String senderUsername,
            @RequestParam MultipartFile file
    ) {
        try {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            Users sender = userRepository.findByUsername(senderUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Authorization check: verify user is a member of the group
            if (!isUserInGroup(sender, group)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not a member of this group"));
            }

            // Validate file type (images only)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Only image files are allowed"));
            }

            // Ensure upload directory exists
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Generate a unique filename
            String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save message to DB
            GroupMessage message = new GroupMessage();
            message.setGroup(group);
            message.setSender(sender);
            message.setMediaUrl(filePath.toString());
            message.setMessage("Image sent");
            message.setCreatedAt(new Date());
            groupMessageRepository.save(message);

            // Broadcast to active WebSocket sessions
            broadcastToWebSocket(groupId, senderUsername, " uploaded an image: " + file.getOriginalFilename());

            // Create notifications for other members
            createNotificationsForMembers(group, sender, "uploaded an image");

            return ResponseEntity.ok(Map.of(
                    "message", "Image uploaded successfully",
                    "messageId", message.getId(),
                    "filePath", message.getMediaUrl(),
                    "fileName", file.getOriginalFilename()
            ));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    /**
     * Get image file by message ID
     */
    @GetMapping("/image/{messageId}")
    public ResponseEntity<?> getImage(@PathVariable Long messageId) {
        try {
            // Find the message by ID
            GroupMessage message = groupMessageRepository.findById(messageId)
                    .orElseThrow(() -> new RuntimeException("Image not found"));

            // Verify it's an image message
            if (message.getMediaUrl() == null || message.getMediaUrl().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "This message does not contain an image"));
            }

            // Read the image file
            Path path = Paths.get(message.getMediaUrl());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = Files.readAllBytes(path);

            // Determine content type from file extension
            String contentType = Files.probeContentType(path);
            if (contentType == null) contentType = "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to read image"));
        }
    }

    /**
     * Check if a user is a member of a group
     */
    private boolean isUserInGroup(Users user, Group group) {
        return group.getMembers().stream()
                .anyMatch(member -> member.getId().equals(user.getId()));
    }

    /**
     * Broadcast message to WebSocket sessions
     */
    private void broadcastToWebSocket(Long groupId, String sender, String message) {
        GroupMessageWebsocket.broadcastToWebSocket(groupId, sender, message);
    }



    /**
     * Create notifications for all group members except sender
     */
    private void createNotificationsForMembers(Group group, Users sender, String action) {
        group.getMembers().stream()
                .filter(member -> !member.getId().equals(sender.getId()))
                .forEach(member -> {
                    Notification notif = new Notification();
                    notif.setUser(member);
                    notif.setTitle("New Group Activity");
                    notif.setMessage(sender.getUsername() + " " + action + " in " + group.getGroupName());
                    notif.setCreatedAt(new Date());
                    notif.setIsRead(false);
                    notificationRepository.save(notif);

                    NotificationWebSocket.pushNotification(member.getUsername(), notif);
                });
    }
}