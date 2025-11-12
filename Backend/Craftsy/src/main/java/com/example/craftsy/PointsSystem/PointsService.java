package com.example.craftsy.PointsSystem;

import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.PointsSystem.Entity.PointsHistory;
import com.example.craftsy.PointsSystem.Entity.UserPoints;
import com.example.craftsy.PointsSystem.Repository.PointsHistoryRepository;
import com.example.craftsy.PointsSystem.Repository.PointsHistoryRepository;
import com.example.craftsy.PointsSystem.Repository.UserPointsRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PointsService {

    @Autowired
    private UserPointsRepository userPointsRepository;

    @Autowired
    private PointsHistoryRepository pointHistoryRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private static final int POINTS_POST = 20;
    private static final int POINTS_TUTORIAL = 20;
    private static final int POINTS_COMMENT = 10;


    /**
     * Award points for posting a project
     */
    @Transactional
    public void awardPointsForPost(Users user, Long postId) {
        awardPoints(user, POINTS_POST, "POST_CREATED", postId
        );
    }

    /**
     * Award points for posting a tutorial
     */
    @Transactional
    public void awardPointsForTutorial(Users user, Long tutorialId) {
        awardPoints(user, POINTS_TUTORIAL, "TUTORIAL_POSTED", tutorialId
        );

        UserPoints userPoints = getUserPoints(user);
        userPoints.setTutorialsCount(userPoints.getTutorialsCount() + 1);
        userPointsRepository.save(userPoints);
    }

    /**
     * Award points for commenting
     */
    @Transactional
    public void awardPointsForComment(Users user, Long commentId) {
        awardPoints(user, POINTS_COMMENT, "COMMENT_ADDED", commentId
        );

        UserPoints userPoints = getUserPoints(user);
        userPoints.setCommentsCount(userPoints.getCommentsCount() + 1);
        userPointsRepository.save(userPoints);
    }



    /**
     *  method to award points
     */
    @Transactional
    public void awardPoints(Users user, Integer points, String actionType, Long referenceId) {
        // Get or create user points
        UserPoints userPoints = getUserPoints(user);
        String previousTier = userPoints.getCurrentTier();

        // Add points
        userPoints.addPoints(points);
        userPoints.setLastUpdated(new Date());
        userPointsRepository.save(userPoints);

        // Save point history
        PointsHistory history = new PointsHistory(user, points, actionType, referenceId);
        pointHistoryRepository.save(history);

        // Check for tier upgrade
        if (!previousTier.equals(userPoints.getCurrentTier())) {
            notifyTierUpgrade(user, userPoints.getCurrentTier());
        }
    }

    /**
     * Get user points, create if doesn't exist
     */
    public UserPoints getUserPoints(Users user) {
        return userPointsRepository.findByUser(user)
                .orElseGet(() -> {
                    UserPoints newPoints = new UserPoints();
                    newPoints.setUser(user);
                    return userPointsRepository.save(newPoints);
                });
    }

    /**
     * Notify user of tier upgrade
     */
    private void notifyTierUpgrade(Users user, String newTier) {
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setTitle(" Tier Upgrade!");
        notif.setMessage("Congratulations! You've reached " + newTier + " tier!");
        notif.setCreatedAt(new Date());
        notif.setIsRead(false);
        notificationRepository.save(notif);

        NotificationWebSocket.pushNotification(user.getUsername(), notif);
    }
}
