package com.example.craftsy.FollowingFollowers.Repository;

import com.example.craftsy.FollowingFollowers.Entity.Notification;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(Users user);
}