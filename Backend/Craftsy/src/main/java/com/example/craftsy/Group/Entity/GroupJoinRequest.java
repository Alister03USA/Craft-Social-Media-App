package com.example.craftsy.Group.Entity;


import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;


/**
 * Represents a pending join request for a private group
 */
@Entity
@Table(name = "group_join_requests")
public class GroupJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "is_accepted")
    private Boolean accepted = false;


    // getters and Setters
    public GroupJoinRequest() {}
    public GroupJoinRequest(Group group, Users user,  Boolean accepted) {
        this.group = group;
        this.user = user;
        this.accepted = accepted;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }



}
