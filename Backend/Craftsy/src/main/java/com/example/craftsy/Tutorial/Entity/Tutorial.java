package com.example.craftsy.Tutorial.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * Represents a tutorial page.
 */
@Entity
@Table(name = "tutorial")
public class Tutorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required.")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @NotBlank(message = "Title is required.")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters.")
    private String title;


    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description;

    @NotBlank(message = "Category is required.")
    @Size(min = 3, max = 50, message = "Category must be between 3 and 50 characters.")
    private String category; // Knitting, woodcrating, etc

    private String fileUrl;

    @Size(max = 255, message = "File name cannot exceed 255 characters.")
    private String fileName;

    @Size(max = 500, message = "File path cannot exceed 500 characters.")
    private String filePath;


    @Size(max = 50, message = "File type cannot exceed 50 characters.")
    private String fileType;

    private boolean isPrivate = false;

    // Getters and Setters
    public Long  getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFileUrl() {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }



    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public void setFileType(String fileType) {
        this.fileType = fileType;
    }


    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }


    public boolean isPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }




}

