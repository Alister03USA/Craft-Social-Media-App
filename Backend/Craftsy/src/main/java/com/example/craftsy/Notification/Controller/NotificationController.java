package com.example.craftsy.Notification.Controller;

import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.Notification.Entity.Notification;
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

        NotificationWebSocket.pushRemoveNotification(
                notification.getUser().getUsername(),
                notification.getId()
        );
        
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
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(required = false) Long referenceId
    ) {
        Users receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        Users sender = senderUsername != null
                ? userRepository.findByUsername(senderUsername).orElse(null)
                : null;

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notification.setCreatedAt(new Date());
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        // Push notification via WebSocket
        NotificationWebSocket.pushNotification(receiverUsername, saved);

        return ResponseEntity.ok(saved);
    }

    // Get all the notifications from a user
    @GetMapping("/{username}")
    public ResponseEntity<List<Notification>> getNotification(@PathVariable String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found"));

        // Fetch all unread notifications
        List<Notification> unreadNotifications = notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

        // Mark all fetched notifications as read
        for (Notification notif : unreadNotifications) {
            notif.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications); // batch save

        return ResponseEntity.ok(unreadNotifications);
    }


}
