package com.example.craftsy.Group.Repository;

import com.example.craftsy.Group.Entity.GroupMessage;
import com.example.craftsy.Group.Entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByGroupOrderByCreatedAtAsc(Group group);

    List<GroupMessage> findByReplyToMessageId(Long messageId);
}
