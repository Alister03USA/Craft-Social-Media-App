package com.example.craftsy.messages.conversations;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.messages.Message;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "direct_convo")
public class DirectConversation implements Conversation {

    @Id
    private String id;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = "D-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    @ManyToMany
    @JoinTable(
            name = "direct_convo_members",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "username")
    )
    private List<Users> members;

    @OneToMany(mappedBy = "directConvo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    private LocalDate dateCreated;

    private LocalDateTime lastMessage;

    public DirectConversation(){}

    public DirectConversation(List<Users> members) {
        this.members = members;
        this.dateCreated = LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Users> getMembers() {
        return members;
    }

    public void setMembers(List<Users> members) {
        this.members = members;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void addMessage(Message message){
        this.lastMessage = message.getDate();
        this.messages.add(message);
    }

    public void removeMessage(Message message){
        this.messages.remove(message);
    }

    @Override
    public LocalDateTime getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(LocalDateTime lastMessage) {
        this.lastMessage = lastMessage;
    }
}
