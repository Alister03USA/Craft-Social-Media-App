package com.example.craftsy.messages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.images.Image;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    private String text;

    private Map<String, Integer> reactions;

    private LocalDateTime date;

    private List<Message> replies;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    public Message(String username, String text) {
        this.sender = username;
        this.text = text;
        this.date = LocalDateTime.now();
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Message> getReplies() {
        return replies;
    }

    public void setReplies(List<Message> replies) {
        this.replies = replies;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Map<String, Integer> getReactions() {
        return reactions;
    }

    public void setReactions(Map<String, Integer> reactions) {
        this.reactions = reactions;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
