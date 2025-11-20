package com.example.craftsy.Challenges.Repository;

import com.example.craftsy.Challenges.Entity.Challenge;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByIsActiveTrue();

    List<Challenge> findByTypeAndIsActiveTrue(String upperCase);

    List<Challenge> findByCategoryAndIsActiveTrue(String category);

    List<Challenge> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate today, LocalDate today1);
}
