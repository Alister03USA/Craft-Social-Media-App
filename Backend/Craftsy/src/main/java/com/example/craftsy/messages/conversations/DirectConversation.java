package com.example.craftsy.messages.conversations;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.messages.Message;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "direct_convo")
public class DirectConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private List<Users> members;

    private List<Message> messages;

    private LocalDate dateCreated;

    public DirectConversation(List<Users> members) {
        this.members = members;
        this.dateCreated = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        this.messages.add(message);
    }

    public void removeMessage(Message message){
        this.messages.remove(message);
    }
}
