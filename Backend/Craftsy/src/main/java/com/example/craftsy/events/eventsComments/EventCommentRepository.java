package com.example.craftsy.events.eventsComments;

import com.example.craftsy.patterns.patternsComments.PatternsComments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventCommentRepository extends JpaRepository<EventComment, Long> {
}
