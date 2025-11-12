package com.example.craftsy.events;

import com.example.craftsy.patterns.Patterns;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
