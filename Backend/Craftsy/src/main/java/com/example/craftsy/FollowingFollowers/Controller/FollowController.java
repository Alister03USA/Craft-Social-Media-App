package com.example.craftsy.FollowingFollowers.Controller;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.FollowingFollowers.Entity.Notification;
import com.example.craftsy.FollowingFollowers.Repository.NotificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * Returns profile info including follow status
     */
    @GetMapping("/{viewerUsername}/profile/{targetUsername}")
    public ResponseEntity<Map<String, Object>> getProfileStatus(
            @PathVariable String viewerUsername,
            @PathVariable String targetUsername) {

        Optional<Users> targetUserOpt = userRepository.findByUsername(targetUsername);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Users targetUser = targetUserOpt.get();
        Optional<Users> viewerUserOpt = userRepository.findByUsername(viewerUsername);

        boolean isFollowing = false;
        boolean isPending = false;

        if (viewerUserOpt.isPresent()) {
            Users viewer = viewerUserOpt.get();
            Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(viewer, targetUser);

            if (followOpt.isPresent()) {
                Follow follow = followOpt.get();
                if ("ACCEPTED".equals(follow.getStatus())) {
                    isFollowing = true;
                } else if ("PENDING".equals(follow.getStatus())) {
                    isPending = true;
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", targetUser.getUsername());
        response.put("displayName", targetUser.getDisplayName());
        response.put("isFollowing", isFollowing);
        response.put("isPending", isPending);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /follow
     * Sends a follow request (creates pending follow)
     * Request body: { "targetUsername": "john_doe" }
     */
    @PostMapping("/follow")
    public ResponseEntity<Map<String, String>> sendFollowRequest(
            @RequestHeader("Username") String followerUsername,
            @RequestBody Map<String, String> request) {

        String targetUsername = request.get("targetUsername");

        Optional<Users> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<Users> targetOpt = userRepository.findByUsername(targetUsername);

        if (followerOpt.isEmpty() || targetOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "User not found");
            return ResponseEntity.badRequest().body(error);
        }

        Users follower = followerOpt.get();
        Users target = targetOpt.get();

        // Check if already following or pending
        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(follower, target);
        if (existingFollow.isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Already following or request pending");
            return ResponseEntity.badRequest().body(error);
        }

        // Create pending follow request
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(target);
        follow.setStatus("PENDING");
        followRepository.save(follow);

        // Create notification for target user
        Notification notification = new Notification();
        notification.setUser(target);
        notification.setTitle("New Follower");
        notification.setMessage(follower.getDisplayName() + " wants to follow you");
        notification.setType("FOLLOW_REQUEST");
        notification.setReferenceId(follow.getId());
        notificationRepository.save(notification);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Follow request sent");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /unfollow
     * Unfollows a user or cancels pending request
     * Request body: { "targetUsername": "john_doe" }
     */
    @PostMapping("/unfollow")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @RequestHeader("Username") String followerUsername,
            @RequestBody Map<String, String> request) {

        String targetUsername = request.get("targetUsername");

        Optional<Users> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<Users> targetOpt = userRepository.findByUsername(targetUsername);

        if (followerOpt.isEmpty() || targetOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Users follower = followerOpt.get();
        Users target = targetOpt.get();

        Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(follower, target);
        if (followOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Not following");
            return ResponseEntity.badRequest().body(error);
        }

        Follow follow = followOpt.get();
        boolean wasAccepted = "ACCEPTED".equals(follow.getStatus());

        // Delete the follow relationship
        followRepository.delete(follow);

        // Update counts only if it was accepted
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
     * POST /notifications/respond
     * Accept or reject a follow request
     * Request body: { "notificationId": 5, "accepted": true }
     */
    @PostMapping("/notifications/respond")
    public ResponseEntity<Map<String, String>> respondToFollowRequest(
            @RequestBody Map<String, Object> request) {

        Long notificationId = Long.valueOf(request.get("notificationId").toString());
        Boolean accepted = (Boolean) request.get("accepted");

        Optional<Notification> notifOpt = notificationRepository.findById(notificationId);
        if (notifOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Notification not found");
            return ResponseEntity.notFound().build();
        }

        Notification notification = notifOpt.get();
        Long followId = notification.getReferenceId();

        Optional<Follow> followOpt = followRepository.findById(followId);
        if (followOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Follow request not found");
            return ResponseEntity.badRequest().body(error);
        }

        Follow follow = followOpt.get();

        if (accepted) {
            // Accept the follow request
            follow.setStatus("ACCEPTED");
            followRepository.save(follow);

            // Update follower counts
            Users follower = follow.getFollower();
            Users following = follow.getFollowing();
            follower.setFollowing(follower.getFollowing() + 1);
            following.setFollowers(following.getFollowers() + 1);
            userRepository.save(follower);
            userRepository.save(following);

            // Mark notification as read
            notification.setIsRead(true);
            notificationRepository.save(notification);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Follow request accepted");
            return ResponseEntity.ok(response);
        } else {
            // Reject the follow request
            followRepository.delete(follow);
            notificationRepository.delete(notification);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Follow request rejected");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * GET /notifications
     * Gets all notifications for a user
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(
            @RequestHeader("Username") String username) {

        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Users user = userOpt.get();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        List<Map<String, Object>> response = notifications.stream()
                .map(notif -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", notif.getId());
                    map.put("title", notif.getTitle());
                    map.put("message", notif.getMessage());
                    map.put("type", notif.getType());
                    map.put("read", notif.getIsRead());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /{username}/followers
     * Gets accepted followers only
     */
    @GetMapping("/{username}/followers")
    public ResponseEntity<List<String>> getFollowers(@PathVariable String username) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        Users user = userOpt.get();
        List<Follow> followersList = followRepository.findByFollowing(user);

        // Filter for accepted followers only
        List<String> followerNames = followersList.stream()
                .filter(f -> "ACCEPTED".equals(f.getStatus()))
                .map(f -> f.getFollower().getUsername())
                .toList();

        return ResponseEntity.ok(followerNames);
    }

    /**
     * GET /{username}/following
     * Gets accepted following only
     */
    @GetMapping("/{username}/following")
    public ResponseEntity<List<String>> getFollowing(@PathVariable String username) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        Users user = userOpt.get();
        List<Follow> followingList = followRepository.findByFollower(user);

        // Filter for accepted following only
        List<String> followingNames = followingList.stream()
                .filter(f -> "ACCEPTED".equals(f.getStatus()))
                .map(f -> f.getFollowing().getUsername())
                .toList();

        return ResponseEntity.ok(followingNames);
    }
}