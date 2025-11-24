package com.example.craftsy.Challenges.Controller;


import com.example.craftsy.Challenges.Entity.Challenge;
import com.example.craftsy.Challenges.Entity.ChallengePost;
import com.example.craftsy.Challenges.Entity.ChallengePostComment;
import com.example.craftsy.Challenges.Entity.ChallengePostLike;
import com.example.craftsy.Challenges.Repository.ChallengePostCommentRepository;
import com.example.craftsy.Challenges.Repository.ChallengePostLikeRepository;
import com.example.craftsy.Challenges.Repository.ChallengePostRepository;
import com.example.craftsy.Challenges.Repository.ChallengeRepository;
import com.example.craftsy.PointsSystem.PointsService;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

// import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class ChallengeController {


    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointsService pointsService;

    @Autowired
    private ChallengePostRepository challengePostRepository;

    @Autowired
    private ChallengePostLikeRepository challengePostLikeRepository;

    @Autowired
    private ChallengePostCommentRepository challengePostCommentRepository;


    /**
     * Create a new challenge
     */
    @Operation(summary = "Create a new challenge", description = "Only CHAMPION users can create challenges")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge created successfully"),
            @ApiResponse(responseCode = "403", description = "Only CHAMPION users can create challenges"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/challenge/{username}/post")
    public ResponseEntity<Map<String, Object>> createChallenge(@PathVariable String username,
                                                               @RequestBody Challenge challenge) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String tier = pointsService.getUserPoints(user).getCurrentTier();

        if (!tier.equals("CHAMPION")) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Only CHAMPION users can create challenges"));
        }

        // Set creator
        challenge.setCreatedBy(user);
        challenge.setIsActive(true);


        challengeRepository.save(challenge);
        return ResponseEntity.ok(Map.of(
                "message", "Challenge created successfully",
                "challengeId", challenge.getId()));
    }


    /**
     * Get all active challenges
     */
    @Operation(summary = "Get all active challenges", description = "Retrieve all challenges that are active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active challenges retrieved")
    })
    @GetMapping("/challenge/active")
    public ResponseEntity<List<Challenge>> getActiveChallenges() {
        List<Challenge> activeChallenges = challengeRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeChallenges);
    }


    /**
     * Participate in a challenge
     */
    @Operation(summary = "Participate in a challenge", description = "User can join an active challenge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participation recorded"),
            @ApiResponse(responseCode = "400", description = "Challenge inactive or user already participating"),
            @ApiResponse(responseCode = "404", description = "User or challenge not found")
    })
    @PostMapping("/challenge/{username}/participate/{challengeId}")
    @Transactional
    public ResponseEntity<Map<String, String>> participateChallenge(@PathVariable String username, @PathVariable long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!challenge.getIsActive()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "This challenge is inactive. You cannot join."));
        }

        if (!challenge.getParticipants().contains(user)) {
            challenge.getParticipants().add(user);
            challengeRepository.save(challenge);
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "User already participating in this challenge"));
        }

        return ResponseEntity.ok(Map.of("message", "Participation recorded"));
    }


    /**
     * Mark challenge as completed and award points
     */
    @Operation(summary = "Complete a challenge", description = "Mark a challenge as completed and award points")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge completed and points awarded"),
            @ApiResponse(responseCode = "400", description = "User not participating or already completed"),
            @ApiResponse(responseCode = "404", description = "User or challenge not found")
    })
    @PostMapping("/challenge/{username}/complete/{challengeId}")
    @Transactional
    public ResponseEntity<Map<String, String>> completeChallenge(
            @PathVariable Long challengeId,
            @PathVariable String username
    ) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!challenge.getParticipants().contains(user)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "User is not participating in this challenge"));
        }

        if (!challenge.getCompletedUsers().contains(user)) {
            challenge.getCompletedUsers().add(user);
            challengeRepository.save(challenge);

            // Award points
            pointsService.awardPointsForChallengeCompletion(user, challenge.getId());
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "User already completed this challenge"));
        }

        return ResponseEntity.ok(Map.of("message", "Challenge marked as completed. Points awarded!"));
    }


    /**
     * filter for a challenge
     * @param type
     * @param category
     * @param status
     * @return
     */
    @Operation(summary = "Filter challenges", description = "Filter challenges by type, category, or ongoing status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered challenges retrieved")
    })
    @GetMapping("/challenge/filter")
    public ResponseEntity<?> getFilteredChallenges(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {

        List<Challenge> challenges;

        if (type != null && !type.isEmpty()) {
            challenges = challengeRepository.findByTypeAndIsActiveTrue(type.toUpperCase());
        } else if (category != null && !category.isEmpty()) {
            challenges = challengeRepository.findByCategoryAndIsActiveTrue(category);
        } else if ("ongoing".equalsIgnoreCase(status)) {
            LocalDate today = LocalDate.now();
            challenges = challengeRepository.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
        } else {
            challenges = challengeRepository.findByIsActiveTrue();
        }

        List<Map<String, Object>> response = challenges.stream().map(challenge -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", challenge.getId());
            map.put("title", challenge.getTitle());
            map.put("description", challenge.getDescription());
            map.put("type", challenge.getType());
            map.put("category", challenge.getCategory());
            map.put("startDate", challenge.getStartDate());
            map.put("endDate", challenge.getEndDate());
            map.put("isActive", challenge.getIsActive());
            map.put("createdBy", Map.of(
                    "id", challenge.getCreatedBy().getId(),
                    "username", challenge.getCreatedBy().getUsername()
            ));
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }


    /**
     * Edit a challenge
     * @param challengeId
     * @param username
     * @param updatedChallenge
     * @return
     */
    @Operation(summary = "Edit a challenge", description = "Only the creator can edit their challenge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge updated successfully"),
            @ApiResponse(responseCode = "403", description = "Only creator can edit this challenge"),
            @ApiResponse(responseCode = "404", description = "User or challenge not found")
    })
    @PutMapping("/challenge/{username}/edit/{challengeId}")
    public ResponseEntity<?> editChallenge(
            @PathVariable Long challengeId,
            @PathVariable String username,
            @RequestBody Challenge updatedChallenge) {

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only creator can edit
        if (!challenge.getCreatedBy().equals(user)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Only the creator can edit this challenge"));
        }


        // Update only non-null fields
        if (updatedChallenge.getTitle() != null && !updatedChallenge.getTitle().isEmpty()) {
            challenge.setTitle(updatedChallenge.getTitle());
        }

        if (updatedChallenge.getDescription() != null && !updatedChallenge.getDescription().isEmpty()) {
            challenge.setDescription(updatedChallenge.getDescription());
        }

        if (updatedChallenge.getType() != null && !updatedChallenge.getType().isEmpty()) {
            challenge.setType(updatedChallenge.getType());
        }

        if (updatedChallenge.getCategory() != null && !updatedChallenge.getCategory().isEmpty()) {
            challenge.setCategory(updatedChallenge.getCategory());
        }

        if (updatedChallenge.getStartDate() != null) {
            challenge.setStartDate(updatedChallenge.getStartDate());
        }

        if (updatedChallenge.getEndDate() != null) {
            challenge.setEndDate(updatedChallenge.getEndDate());
        }

        challengeRepository.save(challenge);

        return ResponseEntity.ok(Map.of("message", "Challenge updated successfully"));
    }




    /**
     * Deactivate a challenge
     */
    @Operation(summary = "Deactivate a challenge", description = "Only the creator can deactivate their challenge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge deactivated successfully"),
            @ApiResponse(responseCode = "403", description = "Only creator can deactivate this challenge"),
            @ApiResponse(responseCode = "404", description = "User or challenge not found")
    })
    @PutMapping("/challenge/{username}/deactivate/{challengeId}")
    public ResponseEntity<Map<String, String>> deactivateChallenge(@PathVariable String username, @PathVariable Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only creator can deactivate
        if (!challenge.getCreatedBy().equals(user)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Only the creator can deactivate this challenge"));
        }

        challenge.setIsActive(false);
        challengeRepository.save(challenge);

        return ResponseEntity.ok(Map.of("message", "Challenge deactivated successfully"));
    }


    /**
     * DELETE a challenge by ID
     * Only the creator (CHAMPION user) can delete their own challenge
     */
    @Operation(summary = "Delete a challenge", description = "Only the creator can delete their challenge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Only creator can delete this challenge"),
            @ApiResponse(responseCode = "404", description = "User or challenge not found")
    })
    @DeleteMapping("/challenge/{username}/delete/{challengeId}")
    public ResponseEntity<Map<String, String>> deleteChallenge(
            @PathVariable Long challengeId,
            @PathVariable String username
    ) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only the creator can delete
        if (!challenge.getCreatedBy().equals(user)) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Only the creator can delete this challenge"));
        }

        challengeRepository.delete(challenge);
        return ResponseEntity.ok(Map.of("message", "Challenge deleted successfully"));
    }


    /**
     * Create a post for the challenge
     * @param challengeId
     * @param username
     * @param description
     * @param file
     * @return
     */
    @Operation(summary = "Create a post for a challenge", description = "User can create a post with optional media after joining the challenge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "User must join the challenge or already submitted a post"),
            @ApiResponse(responseCode = "404", description = "User or challenge not found")
    })
    @PostMapping(value = "/challenge/{username}/create/posts/{challengeId}",
    consumes =MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createChallengePost(
            @PathVariable Long challengeId,
            @PathVariable String username,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile file) {

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is participating
        if (!challenge.getParticipants().contains(user)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You must join the challenge first"));
        }

        // Check if already submitted
        if (challengePostRepository.existsByUserIdAndChallengeId(user.getId(), challengeId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You've already submitted a post for this challenge"));
        }

        String mediaUrl = null;
        if (file != null && !file.isEmpty()) {
            mediaUrl = uploadFile(file);
        }

        ChallengePost post = new ChallengePost();
        post.setUser(user);
        post.setChallenge(challenge);
        post.setDescription(description);
        post.setMediaUrl(mediaUrl);
        post.setCreatedAt(LocalDateTime.now());

        challengePostRepository.save(post);

        return ResponseEntity.ok(Map.of(
                "message", "Post created successfully",
                "postId", post.getId()
        ));
    }


    private String uploadFile(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            String uploadDir = "uploads/challenge-posts/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return the URL path (you'll need to serve this via a static resource handler)
            return "/uploads/challenge-posts/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }


    /**
     * Get all the posts from a challenge
     * @param challengeId
     * @return
     */
    @Operation(summary = "Get all posts of a challenge", description = "Retrieve all posts associated with a challenge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Challenge not found")
    })
    @GetMapping("/challenge/{challengeId}/posts")
    public ResponseEntity<?> getChallengePosts(@PathVariable Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        List<ChallengePost> posts = challengePostRepository
                .findByChallengeIdOrderByCreatedAtDesc(challengeId);

        List<Map<String, Object>> response = posts.stream().map(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("description", post.getDescription());
            map.put("mediaUrl", post.getMediaUrl());
            map.put("createdAt", post.getCreatedAt());
            map.put("user", Map.of(
                    "id", post.getUser().getId(),
                    "username", post.getUser().getUsername()
            ));
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            return map;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "challenge", challenge,
                "posts", response
        ));
    }





    /**
     *
     * @param postId
     * @param username
     * @return
     */
    @PostMapping("/challenge/{username}/like/{postId}")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            @PathVariable String username) {

        ChallengePost post = challengePostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (challengePostLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Already liked this post"));
        }

        ChallengePostLike like = new ChallengePostLike();
        like.setPost(post);
        like.setUser(user);
        challengePostLikeRepository.save(like);

        // Update like count
        post.setLikeCount(post.getLikeCount() + 1);
        challengePostRepository.save(post);

        return ResponseEntity.ok(Map.of(
                "message", "Post liked",
                "likeCount", post.getLikeCount()
        ));
    }


    /**
     * Unlike a post
     * @param postId
     * @param username
     * @return
     */
    @Transactional
    @DeleteMapping("/challenge/{username}/unlike/{postId}")
    public ResponseEntity<?> unlikePost(
            @PathVariable Long postId,
            @PathVariable String username) {

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!challengePostLikeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Haven't liked this post"));
        }

        challengePostLikeRepository.deleteByPostIdAndUserId(postId, user.getId());

        ChallengePost post = challengePostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        challengePostRepository.save(post);

        return ResponseEntity.ok(Map.of(
                "message", "Post unliked",
                "likeCount", post.getLikeCount()
        ));
    }


    /**
     * Post a comment
     * @param postId
     * @param username
     * @param comment
     * @return
     */
    @PostMapping("/challenge/{username}/comment/{postId}")
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @PathVariable String username,
            @RequestBody String comment) {

        ChallengePost post = challengePostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChallengePostComment newComment = new ChallengePostComment();
        newComment.setPost(post);
        newComment.setUser(user);
        newComment.setComment(comment);
        challengePostCommentRepository.save(newComment);

        // Update comment count
        post.setCommentCount(post.getCommentCount() + 1);
        challengePostRepository.save(post);

        return ResponseEntity.ok(Map.of(
                "message", "Comment added",
                "commentId", newComment.getId()
        ));
    }


    /**
     * Get all comments of a post
     * @param postId
     * @return
     */
    @GetMapping("/challenge/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        List<ChallengePostComment> comments = challengePostCommentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        List<Map<String, Object>> response = comments.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("comment", c.getComment());
            map.put("createdAt", c.getCreatedAt());
            map.put("user", Map.of(
                    "id", c.getUser().getId(),
                    "username", c.getUser().getUsername()
            ));
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }






}
