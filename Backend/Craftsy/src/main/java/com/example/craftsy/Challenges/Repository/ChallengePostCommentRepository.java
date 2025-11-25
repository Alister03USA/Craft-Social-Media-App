package com.example.craftsy.Challenges.Repository;

import com.example.craftsy.Challenges.Entity.ChallengePostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengePostCommentRepository extends JpaRepository<ChallengePostComment, Long> {
    List<ChallengePostComment> findByPostIdOrderByCreatedAtDesc(Long postId);
    Long countByPostId(Long postId);

}
