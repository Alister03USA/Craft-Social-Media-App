package com.example.craftsy.messages;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.craftsy.images.Image;
import com.example.craftsy.messages.conversations.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    private String text;

    @ElementCollection
    @CollectionTable(
            name = "message_reactions",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @MapKeyColumn(name = "reaction_type")
    @Column(name = "count")
    private Map<String, Integer> reactions = new HashMap<>();

    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "parent_message_id") // foreign key column
    @JsonIgnore
    private Message parentMessage;

    @OneToMany(mappedBy = "parentMessage", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Message> replies = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @ManyToOne
    @JoinColumn(name = "group_convo_id")
    @JsonIgnore
    private GroupConversation groupConvo;

    @ManyToOne
    @JoinColumn(name = "direct_convo_id")
    @JsonIgnore
    private DirectConversation directConvo;

    public Message(){}

    public Message(String username, String text, Conversation convo) {
        if(convo instanceof  GroupConversation){
            this.groupConvo = (GroupConversation) convo;
        }
        else{
            this.directConvo = (DirectConversation) convo;
        }
        this.sender = username;
        this.text = text;
        this.date = LocalDateTime.now();
        reactions = new HashMap<>();
    }

    public Message(String username, String text, Conversation convo, Message parentMessage) {
        if(convo instanceof  GroupConversation){
            this.groupConvo = (GroupConversation) convo;
        }
        else{
            this.directConvo = (DirectConversation) convo;
        }
        this.sender = username;
        this.text = text;
        this.date = LocalDateTime.now();
        this.parentMessage = parentMessage;
        reactions = new HashMap<>();
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

    public void addReply(Message reply){
        this.replies.add(reply);
    }

    public void removeReply(Message reply){
        this.replies.remove(reply);
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

    public void addReaction(String reaction){
        if(this.reactions.containsKey(reaction)){
            int oldVal = this.reactions.get(reaction);
            this.reactions.replace(reaction, oldVal, oldVal + 1);
        }
        else{
            this.reactions.put(reaction, 1);
        }
    }

    public void removeReaction(String reaction){
        if(this.reactions.containsKey(reaction)){
            if(this.reactions.get(reaction) <= 1){
                this.reactions.remove(reaction);
            }
            int oldVal = this.reactions.get(reaction);
            this.reactions.replace(reaction, oldVal, oldVal - 1);
        }
    }

    public Message getParentMessage() {
        return parentMessage;
    }

    public void setParentMessage(Message parentMessage) {
        this.parentMessage = parentMessage;
    }

    public GroupConversation getGroupConvo() {
        return groupConvo;
    }

    public void setGroupConvo(GroupConversation groupConvo) {
        this.groupConvo = groupConvo;
    }

    public DirectConversation getDirectConvo() {
        return directConvo;
    }

    public void setDirectConvo(DirectConversation directConvo) {
        this.directConvo = directConvo;
    }
}
