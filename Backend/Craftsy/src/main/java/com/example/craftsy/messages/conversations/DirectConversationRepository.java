package com.example.craftsy.messages.conversations;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectConversationRepository extends JpaRepository<DirectConversation, Long> {

    Optional<DirectConversation> findById(String id);
}
