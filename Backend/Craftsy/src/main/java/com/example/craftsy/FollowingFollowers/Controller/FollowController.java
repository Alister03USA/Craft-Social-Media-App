package com.example.craftsy.FollowingFollowers.Controller;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Entity.Notification;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.FollowingFollowers.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Comments
@RestController
public class FollowController {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * GET /{viewerUsername}/profile/{targetUsername}
     * Example: /alister_gan/profile/Zayden
     *
     * This endpoint checks the relationship between the viewer and the target user.
     * - If viewer is following the target user, isFollowing = true
     * - If viewer has sent a pending follow request, isPending = true
     * - Otherwise, both are false
     */
    @GetMapping("/{viewerUsername}/profile/{targetUsername}")
    public ResponseEntity<Map<String, Object>> getProfileStatus(
            @PathVariable String viewerUsername,
            @PathVariable String targetUsername) {

        // Look up the target user by username
        Optional<Users> targetUserOpt = userRepository.findByUsername(targetUsername);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Users targetUser = targetUserOpt.get();

        // Look up the viewer user by username
        Optional<Users> viewerUserOpt = userRepository.findByUsername(viewerUsername);

        boolean isFollowing = false; // default: not following
        boolean isPending = false;   // default: no pending request

        if (viewerUserOpt.isPresent()) {
            Users viewer = viewerUserOpt.get();

            // Find if a follow relationship exists
            Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(viewer, targetUser);

            if (followOpt.isPresent()) {
                Follow follow = followOpt.get();

                // If accepted -> following
                if (follow.isAccepted()) {
                    isFollowing = true;
                } else {
                    // Otherwise -> pending
                    isPending = true;
                }
            }
        }

        // Prepare response JSON
        Map<String, Object> response = new HashMap<>();
        response.put("username", targetUser.getUsername());
        response.put("displayName", targetUser.getDisplayName());
        response.put("isFollowing", isFollowing);
        response.put("isPending", isPending);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /{followerUsername}/follow/{targetUsername}
     * Sends a follow request to the target user.
     * Creates a pending follow and a notification for the target.
     */
    @PostMapping("{followerUsername}/follow/{targetUsername}")
    public ResponseEntity<Map<String, String>> sendFollowRequest(
            @PathVariable String followerUsername,
            @PathVariable String targetUsername) {

        // Fetch follower and target users
        Optional<Users> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<Users> targetOpt = userRepository.findByUsername(targetUsername);

        // Return error if any user not found
        if (followerOpt.isEmpty() || targetOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "User not found");
            return ResponseEntity.badRequest().body(error);
        }

        Users follower = followerOpt.get();
        Users target = targetOpt.get();

        // Check if follow relationship already exists
        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(follower, target);
        if (existingFollow.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Already following or request pending");
            return ResponseEntity.badRequest().body(error);
        }

        // Create new follow request
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(target);
        follow.setAccepted(false); // pending
        followRepository.save(follow);

        // Create notification for the target user
        Notification notification = new Notification();
        notification.setUser(target);
        notification.setTitle("New Follower");
        notification.setMessage(follower.getDisplayName() + " wants to follow you");
        notification.setType("FOLLOW_REQUEST");
        notification.setReferenceId(follow.getId()); // link to follow request
        notificationRepository.save(notification);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Follow request sent");
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /{followerUsername}/unfollow/{targetUsername}
     * Unfollows a user or cancels a pending follow request.
     * Adjusts follower/following counts if it was accepted.
     */
    @DeleteMapping("{followerUsername}/unfollow/{targetUsername}")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @PathVariable String followerUsername,
            @PathVariable String targetUsername) {

        // Fetch users
        Optional<Users> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<Users> targetOpt = userRepository.findByUsername(targetUsername);

        if (followerOpt.isEmpty() || targetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Users follower = followerOpt.get();
        Users target = targetOpt.get();

        // Fetch follow relationship
        Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(follower, target);
        if (followOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Not following");
            return ResponseEntity.badRequest().body(error);
        }

        Follow follow = followOpt.get();
        boolean wasAccepted = follow.isAccepted();

        // Delete follow relationship
        followRepository.delete(follow);

        // Update follower/following counts if accepted
        if (wasAccepted) {
            follower.setFollowing(Math.max(0, follower.getFollowing() - 1));
            target.setFollowers(Math.max(0, target.getFollowers() - 1));
            userRepository.save(follower);
            userRepository.save(target);
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User unfollowed");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /notifications/{username}
     * Retrieves all unread notifications for the specified user.
     * Only one follow request notification per follower is included.
     */
    @GetMapping("/notifications/{username}")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(
            @PathVariable String username) {

        // Fetch the user by username
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // Return 404 if user not found
        }
        Users user = userOpt.get();

        // Fetch unread notifications ordered by newest first
        List<Notification> notifications = notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

        // Prepare response list
        List<Map<String, Object>> response = new ArrayList<>();
        Set<Long> seenFollowerIds = new HashSet<>(); // Track which followers we've already included

        for (Notification notif : notifications) {
            // If this is not a FOLLOW_REQUEST notification, include it directly
            if (!"FOLLOW_REQUEST".equals(notif.getType())) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", notif.getId());
                map.put("title", notif.getTitle());
                map.put("message", notif.getMessage());
                map.put("read", notif.getIsRead());
                response.add(map);
                continue; // Skip to next notification
            }

            // For FOLLOW_REQUEST notifications, fetch the associated Follow entity
            Optional<Follow> followOpt = followRepository.findById(notif.getReferenceId());
            if (followOpt.isEmpty()) continue; // Skip if follow request not found

            Users follower = followOpt.get().getFollower();
            if (follower == null || seenFollowerIds.contains(follower.getId())) {
                // Skip if follower is null or we've already included a notification from this follower
                continue;
            }

            // Mark this follower as seen
            seenFollowerIds.add(follower.getId());

            // Add notification to response
            Map<String, Object> map = new HashMap<>();
            map.put("id", notif.getId());
            map.put("title", notif.getTitle());
            map.put("message", notif.getMessage());
            map.put("read", notif.getIsRead());
            response.add(map);
        }

        // Return the final filtered list of notifications
        return ResponseEntity.ok(response);
    }




    /**
     * PUT /notifications/respond/{targetUsername}/{followerUsername}/{accepted}
     * Accept or reject a follow request from a specific follower
     */
    @PutMapping("/notifications/respond/{notificationId}/{accepted}")
    public ResponseEntity<Map<String, String>> respondToFollowRequest(
            @PathVariable Long notificationId,
            @PathVariable boolean accepted) {

        // Find the notification
        Optional<Notification> notifOpt = notificationRepository.findById(notificationId);
        if (notifOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", "Notification not found"
            ));
        }

        Notification notification = notifOpt.get();
        Long followId = notification.getReferenceId();

        // Find the follow request
        Optional<Follow> followOpt = followRepository.findById(followId);
        if (followOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Follow request not found"
            ));
        }

        Follow follow = followOpt.get();

        if (accepted) {
            // Accept the follow request
            follow.setAccepted(true);
            followRepository.save(follow);

            // Update follower counts
            Users follower = follow.getFollower();
            Users target = follow.getFollowing();
            follower.setFollowing(follower.getFollowing() + 1);
            target.setFollowers(target.getFollowers() + 1);
            userRepository.save(follower);
            userRepository.save(target);

            // Mark notification as read
            notification.setIsRead(true);
            notificationRepository.save(notification);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Follow request accepted"
            ));
        } else {
            // Reject the follow request
            followRepository.delete(follow);
            notificationRepository.delete(notification);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Follow request rejected"
            ));
        }
    }



    /**
     * GET /{username}/following
     * Retrieves a list of users that the given username is following (accepted only)
     */
    @GetMapping("/{username}/following")
    public ResponseEntity<List<String>> getFollowing(@PathVariable String username) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Users user = userOpt.get();
        List<Follow> followingList = followRepository.findByFollower(user);

        // Collect accepted following usernames
        List<String> followingNames = new ArrayList<>();
        for (Follow f : followingList) {
            if (f.isAccepted()) {
                String followingUsername = f.getFollowing().getUsername();
                if (followingUsername != null) {
                    followingNames.add(followingUsername);
                }
            }
        }

        return ResponseEntity.ok(followingNames);
    }

    /**
     * GET /{username}/followers
     * Retrieves a list of users who follow the given username (accepted only)
     */
    @GetMapping("/{username}/followers")
    public ResponseEntity<List<String>> getFollowers(@PathVariable String username) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Users user = userOpt.get();
        List<Follow> followersList = followRepository.findByFollowing(user);

        // Collect accepted follower usernames
        List<String> followerNames = new ArrayList<>();
        for (Follow f : followersList) {
            if (f.isAccepted()) {
                String followerUsername = f.getFollower().getUsername();
                if (followerUsername != null) {
                    followerNames.add(followerUsername);
                }
            }
        }

        return ResponseEntity.ok(followerNames);
    }
}
