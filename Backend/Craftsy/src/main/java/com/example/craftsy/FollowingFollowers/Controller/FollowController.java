package com.example.craftsy.FollowingFollowers.Controller;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * <p>
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

        // Notify TARGET user (the one being followed)
        Notification targetNotification = new Notification();
        targetNotification.setUser(target); // receiver
        targetNotification.setSender(follower); // sender
        targetNotification.setTitle("New Follow Request");
        targetNotification.setMessage(follower.getDisplayName() + " sent you a follow request.");
        targetNotification.setType("FOLLOW_REQUEST");
        targetNotification.setReferenceId(follow.getId());
        targetNotification.setReferenceType("FOLLOW");
        targetNotification.setIsRead(false);
        targetNotification.setCreatedAt(new Date());
        Notification savedTargetNotification = notificationRepository.save(targetNotification);

        // Real-time push to TARGET user
        NotificationWebSocket.pushNotification(target.getUsername(), savedTargetNotification);

        // Notify SENDER that request was successfully sent
        Notification senderNotification = new Notification();
        senderNotification.setUser(follower); // receiver = sender
        senderNotification.setSender(target); // for clarity (target is context)
        senderNotification.setTitle("Follow Request Sent");
        senderNotification.setMessage("Your follow request to " + target.getDisplayName() + " has been sent.");
        senderNotification.setType("FOLLOW_REQUEST_SENT");
        senderNotification.setReferenceId(follow.getId());
        senderNotification.setReferenceType("FOLLOW");
        senderNotification.setIsRead(false);
        senderNotification.setCreatedAt(new Date());
        Notification savedSenderNotification = notificationRepository.save(senderNotification);

        // Real-time push to SENDER user
        NotificationWebSocket.pushNotification(follower.getUsername(), savedSenderNotification);

        return ResponseEntity.ok(Map.of("message", "Follow request sent successfully"));
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
     * PUT /notifications/respond/{notificationId}/{accepted}
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

        // mark the original follow request as read
        notification.setIsRead(true);
        notificationRepository.save(notification);

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
        Users sender = follow.getFollower(); // user who sent the request
        Users target = follow.getFollowing(); // user who received the request

        if (accepted) {
            follow.setAccepted(true);
            followRepository.save(follow);

            sender.setFollowing(sender.getFollowing() + 1);
            target.setFollowers(target.getFollowers() + 1);
            userRepository.save(sender);
            userRepository.save(target);

            // Notify sender that their request was accepted
            Notification acceptedNotification = new Notification();
            acceptedNotification.setUser(sender);
            acceptedNotification.setSender(target);
            acceptedNotification.setTitle("Follow Request Accepted");
            acceptedNotification.setMessage(target.getDisplayName() + " accepted your follow request!");
            acceptedNotification.setType("FOLLOW_ACCEPTED");
            acceptedNotification.setReferenceId(follow.getId());
            acceptedNotification.setReferenceType("FOLLOW");
            acceptedNotification.setIsRead(false);
            acceptedNotification.setCreatedAt(new Date());
            Notification savedAccepted = notificationRepository.save(acceptedNotification);

            NotificationWebSocket.pushNotification(sender.getUsername(), savedAccepted);

            return ResponseEntity.ok(Map.of("message", "Follow request accepted"));
        } else {
            followRepository.delete(follow);

            // Notify sender that their request was declined
            Notification declinedNotification = new Notification();
            declinedNotification.setUser(sender);
            declinedNotification.setSender(target);
            declinedNotification.setTitle("Follow Request Declined");
            declinedNotification.setMessage(target.getDisplayName() + " declined your follow request.");
            declinedNotification.setType("FOLLOW_DECLINED");
            declinedNotification.setReferenceId(notification.getReferenceId());
            declinedNotification.setReferenceType("FOLLOW");
            declinedNotification.setIsRead(false);
            declinedNotification.setCreatedAt(new Date());
            Notification savedDeclined = notificationRepository.save(declinedNotification);

            NotificationWebSocket.pushNotification(sender.getUsername(), savedDeclined);

            return ResponseEntity.ok(Map.of("message", "Follow request declined"));
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
