package com.example.craftsy.Group.Controller;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Entity.GroupMessage;
import com.example.craftsy.Group.Repository.GroupMessageRepository;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.Group.GroupMessageWebsocket;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;


    /**
     * Get message history for a group
     */
    @Operation(summary = "Get group message history",
            description = "Retrieve the message history for a specific group. Only group members can access messages.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns the list of messages"),
            @ApiResponse(responseCode = "403", description = "User is not a member of this group"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
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
     */
    @Operation(summary = "Upload an image to a group",
            description = "Allows a member to upload an image to the group. Other members are notified.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file type or user not in group"),
            @ApiResponse(responseCode = "500", description = "Server error while saving file")
    })
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
            createNotificationsForMembers(group, sender, "uploaded an image", message.getId());

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
    @Operation(summary = "Get uploaded image by message ID",
            description = "Retrieve the image associated with a specific message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns the image bytes"),
            @ApiResponse(responseCode = "400", description = "Message does not contain an image"),
            @ApiResponse(responseCode = "404", description = "Image file not found"),
            @ApiResponse(responseCode = "500", description = "Error reading the image file")
    })
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

    // Get message for reply
    @Operation(summary = "Get comments for a message",
            description = "Retrieve all reply messages (comments) for a specific message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns list of comments")
    })
    @GetMapping("/{messageId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getComments(@PathVariable Long messageId) {
        List<GroupMessage> comments = groupMessageRepository.findByReplyToMessageId(messageId);
        List<Map<String, Object>> response = comments.stream().map(c -> Map.of(
                "sender", Map.of("username", c.getSender().getUsername()),
                "comment", c.getMessage()
        )).toList();
        return ResponseEntity.ok(response);
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
    private void createNotificationsForMembers(Group group, Users sender, String action, Long referenceId) {
        group.getMembers().stream()
                .filter(member -> !member.getId().equals(sender.getId()))
                .forEach(member -> {
                    Notification notif = new Notification();
                    notif.setUser(member);
                    notif.setTitle("New Group Activity");
                    notif.setMessage(sender.getUsername() + " " + action + " in " + group.getGroupName());
                    notif.setCreatedAt(new Date());
                    notif.setReferenceId(referenceId);
                    notif.setIsRead(false);
                    notificationRepository.save(notif);

                    NotificationWebSocket.pushNotification(member.getUsername(), notif);
                });
    }
}
