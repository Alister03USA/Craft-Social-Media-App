package com.example.craftsy.feed;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "feed",
        uniqueConstraints = {
                // The unique constraint now uses the foreign key column (user_fk_username)
                // and the project name column
                @UniqueConstraint(columnNames = {"user_fk_username", "projectName"}, name = "UK_project_name_username")
        })
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_fk_username",        // 1. The name of the foreign key column in the 'projects' table
            nullable = false,
            referencedColumnName = "username"  // 2. The column name in the 'users' table to reference
    )
    private Users user;

    @Column(name = "project_name", nullable = false)
    private String projectName;
    private String projectType;
    private String supplies;
    private String projectDesc;
    private String projectPic;
    private String visibility;

    @Column(nullable = false)
    private LocalDateTime date;

    public Feed() {
    }

    public String getUsername() {
        return user.getUsername();
    }

    public void setUsername(String username) {
        this.user.setUsername(username);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getSupplies() {
        return supplies;
    }

    public void setSupplies(String supplies) {
        this.supplies = supplies;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public String getProjectPic() {
        return projectPic;
    }

    public void setProjectPic(String projectPic) {
        this.projectPic = projectPic;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
