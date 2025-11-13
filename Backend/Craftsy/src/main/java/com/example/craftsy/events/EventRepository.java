package com.example.craftsy.events;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<List<Event>> findByRsvpYesContaining(Users user);
    Optional<List<Event>> findByGroup(Group group);
    Optional<List<Event>> findByEventNameContaining(String eventName);

}
