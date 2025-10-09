package com.example.craftsy.FollowingFollowers.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import java.util.Date;

/**
 * Notification entity represents a notification for a user,
 * e.g., follow request, like, or comment.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who receives the notification
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Title of the notification
    @Column(nullable = false)
    private String title;

    // Message content
    @Column(nullable = false)
    private String message;

    // Type of notification: "FOLLOW_REQUEST", "LIKE", "COMMENT"
    @Column(nullable = false)
    private String type;

    // References Follow ID or other entity
    private Long referenceId;

    // Has the user read the notification?
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    // Timestamp when the notification was created
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt = new Date();

    // Default constructor
    public Notification() {}

    // Full constructor
    public Notification(Users user, String title, String message, String type, Long referenceId, Boolean isRead, Date createdAt) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
