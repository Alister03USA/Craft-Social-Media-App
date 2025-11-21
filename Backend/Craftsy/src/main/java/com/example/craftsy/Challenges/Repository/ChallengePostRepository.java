package com.example.craftsy.Challenges.Repository;

import com.example.craftsy.Challenges.Entity.ChallengePost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChallengePostRepository extends JpaRepository<ChallengePost, Long> {
    List<ChallengePost> findByChallengeIdOrderByCreatedAtDesc(Long challengeId);
    List<ChallengePost> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndChallengeId(Long userId, Long challengeId);
}