package com.example.craftsy.PointsSystem.Controller;

import com.example.craftsy.PointsSystem.Entity.PointsHistory;
import com.example.craftsy.PointsSystem.Entity.UserPoints;
import com.example.craftsy.PointsSystem.Repository.PointsHistoryRepository;
import com.example.craftsy.PointsSystem.Repository.UserPointsRepository;
import com.example.craftsy.PointsSystem.PointsService;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private UserPointsRepository userPointsRepository;

    @Autowired
    private PointsHistoryRepository pointHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /points/{username}
     * Get user's current points and tier
     */
    @GetMapping("/points/{username}")
    public ResponseEntity<?> getUserPoints(@PathVariable String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPoints userPoints = pointsService.getUserPoints(user);

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("totalPoints", userPoints.getTotalPoints());
        response.put("currentTier", userPoints.getCurrentTier());
        response.put("postsCount", userPoints.getPostsCount());
        response.put("tutorialsCount", userPoints.getTutorialsCount());
        response.put("commentsCount", userPoints.getCommentsCount());
        response.put("lastUpdated", userPoints.getLastUpdated());

        // Calculate progress to next tier
        response.put("nextTier", getNextTier(userPoints.getCurrentTier()));
        response.put("pointsToNextTier", getPointsToNextTier(userPoints.getTotalPoints()));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /points/{username}/history
     * Get user's point history
     */
    @GetMapping("/points/{username}/history")
    public ResponseEntity<?> getPointHistory(@PathVariable String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PointsHistory> history = pointHistoryRepository.findByUserOrderByCreatedAtDesc(user);

        List<Map<String, Object>> response = history.stream().map(h -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", h.getId());
            entry.put("points", h.getPointsEarned());
            entry.put("action", h.getActionType());
            entry.put("referenceId", h.getReferenceId());  // Add reference
            entry.put("date", h.getCreatedAt());
            return entry;
        }).toList();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /points/leaderboard
     * Get top 10 users by points
     */
    @GetMapping("/points/leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        List<UserPoints> topUsers = userPointsRepository.findTop10ByOrderByTotalPointsDesc();

        List<Map<String, Object>> response = topUsers.stream().map(up -> {
            Map<String, Object> map = new HashMap<>();
            map.put("username", up.getUser().getUsername());
            map.put("totalPoints", up.getTotalPoints());
            map.put("tier", up.getCurrentTier());
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /points/tiers
     * Get all tier information
     */
    @GetMapping("/points/tiers")
    public ResponseEntity<?> getTierInfo() {
        List<Map<String, Object>> tiers = Arrays.asList(
                Map.of("name", "BEGINNER", "minPoints", 0, "maxPoints", 99),
                Map.of("name", "INTERMEDIATE", "minPoints", 100, "maxPoints", 199),
                Map.of("name", "EXPERT", "minPoints", 200, "maxPoints", 299),
                Map.of("name", "CHAMPION", "minPoints", 300, "maxPoints", Integer.MAX_VALUE)
        );

        return ResponseEntity.ok(tiers);
    }

    // Helper methods
    private String getNextTier(String currentTier) {
        switch (currentTier) {
            case "BEGINNER": return "INTERMEDIATE";
            case "INTERMEDIATE": return "EXPERT";
            case "EXPERT": return "CHAMPION";
            case "CHAMPION": return "MAX LEVEL";
            default: return "UNKNOWN";
        }
    }

    private Integer getPointsToNextTier(Integer currentPoints) {
        if (currentPoints < 100) return 100 - currentPoints;
        if (currentPoints < 200) return 200 - currentPoints;
        if (currentPoints < 300) return 300 - currentPoints;
        return 0;  // Max level
    }
}
