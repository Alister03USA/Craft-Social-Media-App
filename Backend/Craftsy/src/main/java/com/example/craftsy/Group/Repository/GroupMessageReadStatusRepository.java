package com.example.craftsy.Group.Repository;

import com.example.craftsy.Group.Entity.GroupMessageReadStatus;
import com.example.craftsy.Group.Entity.GroupMessage;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupMessageReadStatusRepository extends JpaRepository<GroupMessageReadStatus, Long> {
    Optional<GroupMessageReadStatus> findByMessageAndUser(GroupMessage message, Users user);
}
