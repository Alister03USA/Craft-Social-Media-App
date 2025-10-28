package com.example.craftsy.messages.conversations;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupConversationRepository extends JpaRepository<GroupConversation, Long> {
    Optional<GroupConversation> findById(String id);
}
