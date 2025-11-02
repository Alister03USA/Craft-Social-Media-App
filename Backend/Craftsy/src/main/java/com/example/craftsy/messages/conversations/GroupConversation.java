package com.example.craftsy.messages.conversations;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.images.Image;
import com.example.craftsy.messages.Message;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "group_convo")
public class GroupConversation implements Conversation{
    @Id
    private String id;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = "G-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    @ManyToMany
    @JoinTable(
            name = "group_convo_members",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<Users> members = new ArrayList<>();

    @OneToMany(mappedBy = "groupConvo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    private LocalDate dateCreated;

    private String groupName;

    @ManyToOne
    @JoinColumn(name = "group_pic_id")
    private Image groupPic;

    private LocalDateTime lastMessage;

    public GroupConversation(){}

    public GroupConversation(List<Users> members) {
        this.members = members;
        this.dateCreated = LocalDate.now();
    }

    public Image getGroupPic() {
        return groupPic;
    }

    public void setGroupPic(Image groupPic) {
        this.groupPic = groupPic;
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

    public void addMember(Users user){
        this.members.add(user);
    }

    public void removeMember(Users user){
        this.members.remove(user);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
