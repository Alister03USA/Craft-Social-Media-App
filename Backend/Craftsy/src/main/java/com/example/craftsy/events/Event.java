package com.example.craftsy.events;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.events.eventsComments.EventComment;
import com.example.craftsy.feed.feedComments.FeedComments;
import com.example.craftsy.images.Image;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventName;

    private LocalDateTime eventDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "host",
            nullable = false,
            referencedColumnName = "username"
    )
    private Users eventHost;

    private LocalDateTime dateCreated;

    private String craftType;

    @ManyToMany
    @JoinTable(
            name = "rsvpYes",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "username")
    )
    private List<Users> rsvpYes;

    @ManyToMany
    @JoinTable(
            name = "rsvpNo",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "username")
    )
    private List<Users> rsvpNo;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToMany(mappedBy = "event")
    @OrderBy("likes DESC")
    @JsonManagedReference
    private List<EventComment> comments;

    public Event() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public Users getEventHost() {
        return eventHost;
    }

    public void setEventHost(Users eventHost) {
        this.eventHost = eventHost;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCraftType() {
        return craftType;
    }

    public void setCraftType(String craftType) {
        this.craftType = craftType;
    }

    public List<Users> getRsvpYes() {
        return rsvpYes;
    }

    public void setRsvpYes(List<Users> rsvpYes) {
        this.rsvpYes = rsvpYes;
    }

    public List<Users> getRsvpNo() {
        return rsvpNo;
    }

    public void setRsvpNo(List<Users> rsvpNo) {
        this.rsvpNo = rsvpNo;
    }

    public List<EventComment> getComments() {
        return comments;
    }

    public void setComments(List<EventComment> comments) {
        this.comments = comments;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void addComment(EventComment comment){
        this.comments.add(comment);
    }

    public void removeComment(EventComment comment){
        this.comments.remove(comment);
    }
}
