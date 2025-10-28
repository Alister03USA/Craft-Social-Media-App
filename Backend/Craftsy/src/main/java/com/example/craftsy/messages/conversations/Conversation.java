package com.example.craftsy.messages.conversations;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.messages.Message;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.List;

public interface Conversation {

    String getId();

    void setId(String id);

    List<Users> getMembers();

    void setMembers(List<Users> members);

    List<Message> getMessages();

    void setMessages(List<Message> messages);

    LocalDate getDateCreated();

    void setDateCreated(LocalDate dateCreated);

    void addMessage(Message message);

    void removeMessage(Message message);
}
