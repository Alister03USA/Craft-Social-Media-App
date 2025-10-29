package com.example.craftsy.Notification.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Users sender;

    // Title of the notification
    @Column(nullable = false)
    private String title;

    // Message content
    @Column(nullable = false)
    private String message;



    // References Follow ID
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
    public Notification(Users user, Users sender, String title, String message, String type, Long referenceId) {
        this.user = user;
        this.sender = sender;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.isRead = false;
        this.createdAt = new Date();
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

    public void setSender(Users sender) {
        this.sender = sender;
    }

    public void setReferenceType(String referenceType) {
    }
    public Users getSender() {
        return sender;
    }



}
