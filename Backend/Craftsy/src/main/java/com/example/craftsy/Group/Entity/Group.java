package com.example.craftsy.Group.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user-created group.
 */
@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupName;

    @ManyToOne
    @JoinColumn(name = "group_admin_id", nullable = false)
    private Users groupAdmin;

    private String description;

    @Column(name = "is_private")
    private boolean isPrivate = false;

    private String craft;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<Users> members = new HashSet<>();

    // ---------------- Constructors ----------------
    public Group() {
        // No-args constructor required by JPA
    }

    public Group(Long id, String groupName, Users groupAdmin, String description,
                 boolean isPrivate, String craft, Set<Users> members) {
        this.id = id;
        this.groupName = groupName;
        this.groupAdmin = groupAdmin;
        this.description = description;
        this.isPrivate = isPrivate;
        this.craft = craft;
        this.members = members != null ? members : new HashSet<>();
    }

    // ---------------- Getters and Setters ----------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Users getGroupAdmin() { return groupAdmin; }
    public void setGroupAdmin(Users groupAdmin) { this.groupAdmin = groupAdmin; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public String getCraft() { return craft; }
    public void setCraft(String craft) { this.craft = craft; }

    public Set<Users> getMembers() { return members; }
    public void setMembers(Set<Users> members) { this.members = members; }
}
