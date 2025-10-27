package com.example.craftsy.Group.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "group_message_read_status")
public class GroupMessageReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private GroupMessage message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Temporal(TemporalType.TIMESTAMP)
    private Date readAt;

    public GroupMessageReadStatus() {}

    public GroupMessageReadStatus(GroupMessage message, Users user, Date readAt) {
        this.message = message;
        this.user = user;
        this.readAt = readAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GroupMessage getMessage() { return message; }
    public void setMessage(GroupMessage message) { this.message = message; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public Date getReadAt() { return readAt; }
    public void setReadAt(Date readAt) { this.readAt = readAt; }
}
