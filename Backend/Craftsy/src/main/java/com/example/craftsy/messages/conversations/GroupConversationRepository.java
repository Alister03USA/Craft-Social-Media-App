package com.example.craftsy.messages.conversations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupConversationRepository extends JpaRepository<GroupConversation, String> {
    Optional<GroupConversation> findById(String id);

    @Query("SELECT c FROM GroupConversation c JOIN FETCH c.members WHERE c.id = :id")
    Optional<GroupConversation> findByIdWithMembers(@Param("id") String id);

    @Query("SELECT g FROM GroupConversation g JOIN g.members m WHERE m.username = :username")
    Optional<List<GroupConversation>> findAllByMemberUsername(@Param("username") String username);
}
