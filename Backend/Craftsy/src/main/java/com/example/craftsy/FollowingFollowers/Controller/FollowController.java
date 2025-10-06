package com.example.craftsy.FollowingFollowers.Controller;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // Container Object in Java - indicate a value might/might not be present

// Spring automatically converts return values (like strings or lists) to HTTP responses (JSON).
@RestController
public class FollowController {

    // Autowired to find the matching interface and inject it here
    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/{follower_username}/follow/{following_username}")
    public ResponseEntity<String> follow(@PathVariable String follower_username,
                                         @PathVariable String following_username) {
        Optional<Users> user_follower = userRepository.findByUsername(follower_username);
        Optional<Users> user_following = userRepository.findByUsername(following_username);

        if (user_follower.isEmpty() || user_following.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Users follower = user_follower.get();
        Users following = user_following.get();

        if (followRepository.findByFollowerAndFollowing(follower, following).isPresent()) {
            return ResponseEntity.badRequest().body("Already following!!");
        }

        // Create new follow
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);

        // Update counts
        follower.setFollowing(follower.getFollowing() + 1);
        following.setFollowers(following.getFollowers() + 1);
        userRepository.save(follower);
        userRepository.save(following);

        return ResponseEntity.ok(follower_username + " now follows " + following_username);
    }

    @DeleteMapping("/{followerUsername}/unfollow/{followingUsername}")
    public ResponseEntity<String> unfollowUser(@PathVariable String followerUsername,
                                               @PathVariable String followingUsername) {
        Optional<Users> user_follower = userRepository.findByUsername(followerUsername);
        Optional<Users> user_following = userRepository.findByUsername(followingUsername);

        if (user_follower.isEmpty() || user_following.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Users follower = user_follower.get();
        Users following = user_following.get();

        Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(follower, following);
        if (followOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Not following yet");
        }

        // Delete relation
        followRepository.delete(followOpt.get());

        // Update counts safely
        follower.setFollowing(Math.max(0, follower.getFollowing() - 1));
        following.setFollowers(Math.max(0, following.getFollowers() - 1));
        userRepository.save(follower);
        userRepository.save(following);

        return ResponseEntity.ok(followerUsername + " unfollowed " + followingUsername);
    }


    // Get all followers of a user
    @GetMapping("/{username}/followers")
    public ResponseEntity<List<String>> getFollowers(@PathVariable String username) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        Users user = userOpt.get();
        List<Follow> followersList = followRepository.findByFollowing(user);

        List<String> followerNames = followersList.stream()
                .map(f -> f.getFollower().getUsername())
                .toList();

        return ResponseEntity.ok(followerNames);
    }

    // Get all users this user is following
    @GetMapping("/{username}/following")
    public ResponseEntity<List<String>> getFollowing(@PathVariable String username) {
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        Users user = userOpt.get();
        List<Follow> followingList = followRepository.findByFollower(user);

        List<String> followingNames = followingList.stream()
                .map(f -> f.getFollowing().getUsername())
                .toList();

        return ResponseEntity.ok(followingNames);
    }



}