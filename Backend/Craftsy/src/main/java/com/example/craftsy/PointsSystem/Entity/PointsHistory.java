package com.example.craftsy.PointsSystem.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.util.Date;

@Entity
@Table(name = "points_history")
public class PointsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull(message = "User must not be null")
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "action_type")
    @NotBlank(message = "Action type cannot be blank")
    @Size(min = 3, max = 50, message = "Action type length must be between 3 and 50")
    private String actionType;  // POST_CREATED, TUTORIAL_POSTED, COMMENT_ADDED

    @Column(name = "reference_id")
    private Long referenceId;  // ID of the post/tutorial/comment



    @Column(name = "created_at")
    @PastOrPresent(message = "Creation time cannot be in the future")
    private Date createdAt = new Date();

    // Constructor
    public PointsHistory() {}

    public PointsHistory(Users user, Integer pointsEarned, String actionType, Long referenceId) {
        this.user = user;
        this.pointsEarned = pointsEarned;
        this.actionType = actionType;
        this.referenceId = referenceId;
        this.createdAt = new Date();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }


    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}