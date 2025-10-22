package com.example.craftsy.patterns.patternsComments;

import com.example.craftsy.patterns.Patterns;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatternsCommentsRepository extends JpaRepository<PatternsComments, Long> {
    List<PatternsComments> findByPatternOrderByLikesDesc(Patterns pattern);
}
