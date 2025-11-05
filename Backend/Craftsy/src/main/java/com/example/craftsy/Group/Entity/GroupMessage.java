package com.example.craftsy.Group.Entity;


import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import org.apache.catalina.User;

import java.util.Date;

@Entity
@Table(name = "group_messages")
public class GroupMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn
    private Group group;

    @ManyToOne
    @JoinColumn
    private Users sender;


    private String message;

    private String mediaUrl;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    public void setId(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    public void setGroup(Group group) {
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }


    public void setSender(Users user) {
        this.sender = user;
    }

    public Users getSender() {
        return sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}

