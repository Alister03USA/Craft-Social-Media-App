package com.example.craftsy.Notification.Repository;

import com.example.craftsy.FollowingFollowers.Entity.Notification;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(Users user);
}
