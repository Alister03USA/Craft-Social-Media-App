package com.example.craftsy.feed;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.feed.feedComments.FeedComments;
import com.example.craftsy.images.Image;
import com.example.craftsy.patterns.patternsComments.PatternsComments;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feed",
        uniqueConstraints = { //ensure projectName must be unique for each username
                @UniqueConstraint(columnNames = {"username", "projectName"}, name = "UK_project_name_username")
        })
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "username", //name of the foreign key column in the 'projects' table
            nullable = false, //must have username column
            referencedColumnName = "username" //column name in the 'users' table to reference
    )
    private Users user;

    @Column(nullable = false)
    private String projectName;
    private String projectType;
    private String supplies;
    private String projectDesc;
    private String projectPic;

    @Column(name = "visibility", nullable = false)
    private String visibility;

    @Column(nullable = false)
    private LocalDateTime date;

    @OneToMany(mappedBy = "feed")
    @OrderBy("likes DESC")
    @JsonManagedReference
    private List<FeedComments> comments;

    @ManyToMany
    @JoinTable(
            name = "feed_images",
            joinColumns = @JoinColumn(name = "feed_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    private List<Image> images = new ArrayList<>();

    public Feed() {
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
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

    public List<FeedComments> getComments() {
        return comments;
    }

    public void setComments(List<FeedComments> comments) {
        this.comments = comments;
    }

    public void addComments(FeedComments comment){
        this.comments.add(comment);
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
}
