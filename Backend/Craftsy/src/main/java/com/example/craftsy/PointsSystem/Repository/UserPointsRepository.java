package com.example.craftsy.PointsSystem.Repository;
import com.example.craftsy.PointsSystem.Entity.UserPoints;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserPointsRepository extends JpaRepository<UserPoints, Long> {
    Optional<UserPoints> findByUser(Users user);
    Optional<UserPoints> findByUserId(Long userId);
    List<UserPoints> findTop10ByOrderByTotalPointsDesc();  // Leaderboard
    List<UserPoints> findByCurrentTier(String tier);
}
