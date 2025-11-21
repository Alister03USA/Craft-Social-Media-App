package com.example.craftsy.PointsSystem.Repository;

import com.example.craftsy.PointsSystem.Entity.PointsHistory;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PointsHistoryRepository extends JpaRepository<PointsHistory,Long>{
    List<PointsHistory> findByUserOrderByCreatedAtDesc(Users user);
    List<PointsHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PointsHistory> findByActionType(String actionType);
}
