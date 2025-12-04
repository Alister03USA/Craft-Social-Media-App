package com.example.craftsy.Tutorial.Repository;

import com.example.craftsy.Tutorial.Entity.TutorialLikes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TutorialLikesRepository extends JpaRepository<TutorialLikes, Long> {

    Optional<TutorialLikes> findByTutorialIdAndUserId(Long tutorialId, Long userId);

    long countByTutorialId(Long tutorialId);

    List<TutorialLikes> findByTutorialId(Long tutorialId);
}
