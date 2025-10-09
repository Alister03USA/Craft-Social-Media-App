package com.example.craftsy.patterns;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatternsRepository extends JpaRepository<Patterns, Long> {
    Optional<Patterns> findByPatternName(String patternName);
}
