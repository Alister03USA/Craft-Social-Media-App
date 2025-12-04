package com.example.craftsy.Tutorial.Entity;

import com.example.craftsy.SignUpDelete.Entity.Users;
import jakarta.persistence.*;

@Entity
@Table(name = "tutorial_likes")
public class TutorialLikes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tutorial_id")
    private Tutorial tutorial;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    public TutorialLikes() {}
    public TutorialLikes(Tutorial tutorial, Users user) {
        this.tutorial = tutorial;
        this.user = user;
    }

    public Long getId() { return id; }
    public Tutorial getTutorial() { return tutorial; }
    public Users getUser() { return user; }
}
