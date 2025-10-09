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
     * Example: /alister_gan/profile/Zayden
     *
     * Used to check if the logged-in user ("viewer")
     * is already following, has a pending request,
     * or not following the target user.
     */
    @GetMapping("/{viewerUsername}/profile/{targetUsername}")
    public ResponseEntity<Map<String, Object>> getProfileStatus(
            @PathVariable String viewerUsername,
            @PathVariable String targetUsername) {

        // This looks up the target user in the database, the person whose profile is being viewed.
        Optional<Users> targetUserOpt = userRepository.findByUsername(targetUsername);
        if (targetUserOpt.isEmpty()) { // returns 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // extract the targetted user
        Users targetUser = targetUserOpt.get();
        Optional<Users> viewerUserOpt = userRepository.findByUsername(viewerUsername);

        boolean isFollowing = false;
        boolean isPending = false;

        if (viewerUserOpt.isPresent()) {
            Users viewer = viewerUserOpt.get(); // check if the follower and following relationship exists
            Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(viewer, targetUser); // returns a follow object

            // Check if the person is already following/sent request by checking the "Status" column
            if (followOpt.isPresent()) {
                Follow follow = followOpt.get();
                if ("ACCEPTED".equals(follow.getStatus())) {
                    isFollowing = true;
                } else if ("PENDING".equals(follow.getStatus())) {
                    isPending = true;
                }
            }
        }

        // Map object= Key : Values as an output to user (JSON)
        Map<String, Object> response = new HashMap<>();
        response.put("username", targetUser.getUsername());
        response.put("displayName", targetUser.getDisplayName());
        response.put("isFollowing", isFollowing);
        response.put("isPending", isPending);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /{followerUsername}/follow/{targetUsername}
     * Sends a follow request (creates pending follow)
     */
    @PostMapping("{followerUsername}/follow/{targetUsername}")
    public ResponseEntity<Map<String, String>> sendFollowRequest(
            @PathVariable String followerUsername,
            @PathVariable String targetUsername) {

        // Extract both follower and targetUser from database
        Optional<Users> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<Users> targetOpt = userRepository.findByUsername(targetUsername);

        // If either User does not exist -> return message
        if (followerOpt.isEmpty() || targetOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "User not found");
            return ResponseEntity.badRequest().body(error);
        }

        // Extract users object
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
        // create a new follow entry in database
        follow.setFollower(follower); // follower_id
        follow.setFollowing(target); // following_id
        follow.setStatus("PENDING");
        followRepository.save(follow);

        // Create notification for target user
        Notification notification = new Notification();
        notification.setUser(target);
        notification.setTitle("New Follower");
        notification.setMessage(follower.getDisplayName() + " wants to follow you");
        notification.setType("FOLLOW_REQUEST");
        notification.setReferenceId(follow.getId()); // links back to follow request
        notificationRepository.save(notification);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Follow request sent");
        return ResponseEntity.ok(response);
    }


    /**
     * Delete /unfollow
     * Unfollows a user or cancels pending request
     */
    @DeleteMapping("{followerUsername}/unfollow/{targetUsername}")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @PathVariable String followerUsername,
            @PathVariable String targetUsername) {

        Optional<Users> followerOpt = userRepository.findByUsername(followerUsername);
        Optional<Users> targetOpt = userRepository.findByUsername(targetUsername);

        // if either Username doesn't exist
        if (followerOpt.isEmpty() || targetOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "User not found");
            return ResponseEntity.notFound().build();
        }

        Users follower = followerOpt.get();
        Users target = targetOpt.get();

        Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(follower, target);
        // if the follower_id and following_id does not exist
        if (followOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Not following");
            return ResponseEntity.badRequest().body(error);
        }

        // retrieves the follow object
        Follow follow = followOpt.get();
        // make sure it is already following (status = ACCEPTED)
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
     * GET /notifications/{username}
     * Gets all notifications for a user
     */
    @GetMapping("/notifications/{username}")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(
            @PathVariable String username) {

        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) { // returns 404 if user does not exist
            return ResponseEntity.notFound().build();
        }

        Users user = userOpt.get();
        // Retrieves all notifications belonging to that user, ordered by createdAt descending
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        // Convert each notification object into a key-value map
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
     * PUT /notifications/respond/{notificationId}/{accepted}
     * Accept or reject a follow request
     */
    @PutMapping("/notifications/respond/{notificationId}/{accepted}")
    public ResponseEntity<Map<String, String>> respondToFollowRequest(
            @PathVariable Long notificationId,
            @PathVariable boolean accepted) {

        // Retrives the notification by its ID
        Optional<Notification> notifOpt = notificationRepository.findById(notificationId);
        if (notifOpt.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Notification not found");
            return ResponseEntity.status(404).body(error);
        }

        // extract the referenceID from Notification that points to a record in follow table
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
            // Delete the pending follow and notification
            followRepository.delete(follow);
            notificationRepository.delete(notification);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Follow request rejected");
            return ResponseEntity.ok(response);
        }
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