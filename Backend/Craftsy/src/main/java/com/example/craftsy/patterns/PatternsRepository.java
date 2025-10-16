package com.example.craftsy.patterns;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.feed.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatternsRepository extends JpaRepository<Patterns, Long> {
    Optional<Patterns> findByUserAndPatternName(Users user, String patternName);
    Optional<List<Patterns>> findByUser(Users user);
    Optional<List<Patterns>> findByPatternNameContaining(String search);
}
