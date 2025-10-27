package com.example.craftsy.Group.Controller;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Entity.GroupMessage;
import com.example.craftsy.Group.Repository.GroupMessageReadStatusRepository;
import com.example.craftsy.Group.Repository.GroupMessageRepository;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/groupMessage")
public class GroupMessageController {

    private static final String UPLOAD_DIR = "uploads/group_media";

    @Autowired private GroupRepository groupRepository;
    @Autowired private GroupMessageRepository groupMessageRepository;
    @Autowired private GroupMessageReadStatusRepository groupMessageReadStatusRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationRepository notificationRepository;


    /**
     * Send a text message to a group
     */
    @PostMapping("/{groupId}/message")
    public ResponseEntity<?> sendTextMessage(
            @PathVariable Long groupId,
            @RequestParam String senderUsername,
            @RequestParam String content
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        Users sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMessage message = new GroupMessage();
        message.setGroup(group);
        message.setSender(sender);
        message.setMessage(content);
        message.setCreatedAt(new Date());
        groupMessageRepository.save(message);

        broadcastGroupMessage(group, message);
        return ResponseEntity.ok(message);
    }

    /**
     * Upload an image or video message to group
     */
    @PostMapping(
            value = "/{groupId}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadMediaMessage(
            @PathVariable Long groupId,
            @RequestParam String senderUsername,
            @RequestParam MultipartFile file
    ) {
        try {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            Users sender = userRepository.findByUsername(senderUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Ensure upload directory exists
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Generate a unique filename
            String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Determine message type
            String contentType = file.getContentType();
            String messageType = "FILE";
            if (contentType != null) {
                if (contentType.startsWith("image")) messageType = "IMAGE";
                else if (contentType.startsWith("video")) messageType = "VIDEO";
            }

            // Save message to DB
            GroupMessage message = new GroupMessage();
            message.setGroup(group);
            message.setSender(sender);
            message.setMessage(file.getOriginalFilename());
            message.setMediaUrl(filePath.toString());
            message.setCreatedAt(new Date());
            groupMessageRepository.save(message);

            broadcastGroupMessage(group, message);

            return ResponseEntity.ok(Map.of(
                    "message", "Media uploaded successfully",
                    "filePath", message.getMediaUrl(),
                    "messageType", messageType
            ));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mark a specific message as read for a user
     */
//    @PostMapping("/read/{messageId}")
//    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId, @RequestParam String username) {}


    /**
     * Broadcast messages to all group members and create notifications
     * @param group
     * @param message
     */
    private void broadcastGroupMessage(Group group, GroupMessage message) {
        group.getMembers().forEach(member -> {
        });
    }


}
