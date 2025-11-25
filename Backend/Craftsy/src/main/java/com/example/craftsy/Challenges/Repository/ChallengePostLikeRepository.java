package com.example.craftsy.Challenges.Repository;

import com.example.craftsy.Challenges.Entity.ChallengePostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengePostLikeRepository extends JpaRepository<ChallengePostLike, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
    Long countByPostId(Long postId);
}
