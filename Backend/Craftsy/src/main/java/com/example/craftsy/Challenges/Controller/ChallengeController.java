package com.example.craftsy.Challenges.Controller;


import com.example.craftsy.Challenges.Entity.Challenge;
import com.example.craftsy.Challenges.Repository.ChallengeRepository;
import com.example.craftsy.PointsSystem.PointsService;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

// import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;

@RestController
public class ChallengeController {


    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointsService pointsService;


    /**
     * Create a new challenge
     */
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
        return ResponseEntity.ok(Map.of("message", "Challenge created successfully"));
    }


    /**
     * Get all active challenges
     */
    @GetMapping("/challenge/active")
    public ResponseEntity<List<Challenge>> getActiveChallenges() {
        List<Challenge> activeChallenges = challengeRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeChallenges);
    }


    /**
     * Participate in a challenge
     */
    @PostMapping("/challenge/{username}/participate/{challengeId}")
    @Transactional
    public ResponseEntity<Map<String, String>> participateChallenge(@PathVariable String username, @PathVariable long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
     * Search for a challenge
     * @param type
     * @param category
     * @param title
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/challenge/search")
    public ResponseEntity<List<Challenge>> searchChallenges(
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "") String category,
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        // Default date range if not provided
        LocalDate start = startDate != null ? startDate : LocalDate.MIN;
        LocalDate end = endDate != null ? endDate : LocalDate.MAX;

        List<Challenge> results = challengeRepository
                .findByIsActiveTrueAndTypeContainingIgnoreCaseAndCategoryContainingIgnoreCaseAndTitleContainingIgnoreCaseAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                        type, category, title, start, end
                );

        return ResponseEntity.ok(results);
    }




    /**
     * Deactivate a challenge
     */
    @PutMapping("/challenge/{challengeId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateChallenge(@PathVariable Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        challenge.setIsActive(false);
        challengeRepository.save(challenge);

        return ResponseEntity.ok(Map.of("message", "Challenge deactivated"));
    }


    /**
     * DELETE a challenge by ID
     * Only the creator (CHAMPION user) can delete their own challenge
     */
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



}
