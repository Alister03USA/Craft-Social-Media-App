package com.example.craftsy.FollowingFollowers.Repository;

import com.example.craftsy.FollowingFollowers.Entity.Notification;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(Users user);
    // Find a notification by the reference ID (e.g., a Follow ID)
    Optional<Notification> findByReferenceId(Long referenceId);
}