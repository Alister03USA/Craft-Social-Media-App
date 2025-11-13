package com.example.craftsy.Challenges.Repository;

import com.example.craftsy.Challenges.Entity.Challenge;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByIsActiveTrue();
    List<Challenge> findByParticipantsContaining(Users user);
}
