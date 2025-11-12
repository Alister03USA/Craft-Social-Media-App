package com.example.craftsy.Tutorial.Repository;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.Tutorial.Entity.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    List<Tutorial> findByUser_UsernameContainingIgnoreCaseOrTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String username, String title, String description, String category
    );

    List<Tutorial> findByUser(Users user);

    List<Tutorial>findByIs_PrivateFalse();
    List<Tutorial> findByIs_PrivateTrue();


}
