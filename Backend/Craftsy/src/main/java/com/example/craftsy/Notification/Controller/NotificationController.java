package com.example.craftsy.Notification.Controller;

import com.example.craftsy.FollowingFollowers.Entity.Notification;
import com.example.craftsy.FollowingFollowers.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;



    /**
     * PUT /notifications/{id}/read
     * Mark a notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok("Notification marked as read");
    }

    /**
     * POST /notifications
     * Create a new notification and push it via WebSocket
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(
            @RequestParam String receiverUsername,
            @RequestParam(required = false) String senderUsername,
            @RequestParam String type,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(required = false) Long referenceId,
            @RequestParam(required = false) String referenceType
    ) {
        Users receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        Users sender = senderUsername != null
                ? userRepository.findByUsername(senderUsername).orElse(null)
                : null;

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setSender(sender);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification.setCreatedAt(new Date());
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        // Push notification via WebSocket
     //   NotificationWebSocket.sendNotification(receiverUsername, saved);

        return ResponseEntity.ok(saved);
    }
}
