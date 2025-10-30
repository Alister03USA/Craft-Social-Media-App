package com.example.craftsy.messages.conversations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DirectConversationRepository extends JpaRepository<DirectConversation, Long> {

    Optional<DirectConversation> findById(String id);
    @Query("SELECT c FROM DirectConversation c JOIN FETCH c.members WHERE c.id = :id")
    Optional<DirectConversation> findByIdWithMembers(@Param("id") String id);

    @Query("SELECT d FROM DirectConversation d JOIN d.members m WHERE m.username = :username")
    Optional<List<DirectConversation>> findAllByMemberUsername(@Param("username") String username);
}
