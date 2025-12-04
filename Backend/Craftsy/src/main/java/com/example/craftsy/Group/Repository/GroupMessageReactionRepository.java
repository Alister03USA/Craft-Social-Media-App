package com.example.craftsy.Group.Repository;

import com.example.craftsy.Group.Entity.GroupMessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMessageReactionRepository extends JpaRepository<GroupMessageReaction, Long> {

    List<GroupMessageReaction> findByMessageId(Long messageId);

    Optional<GroupMessageReaction> findByMessageIdAndUserId(Long messageId, Long userId);
}
