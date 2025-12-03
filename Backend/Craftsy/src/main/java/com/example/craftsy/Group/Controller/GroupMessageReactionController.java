package com.example.craftsy.Group.Controller;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Entity.GroupMessage;
import com.example.craftsy.Group.Entity.GroupMessageReaction;
import com.example.craftsy.Group.Repository.GroupMessageReactionRepository;
import com.example.craftsy.Group.Repository.GroupMessageRepository;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class GroupMessageReactionController {

    @Autowired private GroupMessageReactionRepository reactionRepository;
    @Autowired private GroupMessageRepository messageRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private UserRepository userRepository;

    // Add or update a reaction (group-specific)
    @PostMapping("/groupMessageReaction/{username}/react/{groupId}/{messageId}")
    public ResponseEntity<?> reactToMessage(
            @PathVariable Long messageId,
            @PathVariable Long groupId,
            @PathVariable String username,
            @RequestParam String reactionType
    ) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is not a member of this group"));
        }

        GroupMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        Optional<GroupMessageReaction> existingReaction =
                reactionRepository.findByMessageIdAndUserId(messageId, user.getId());

        GroupMessageReaction reaction;
        if (existingReaction.isPresent()) {
            reaction = existingReaction.get();
            reaction.setReactionType(reactionType);
            reaction.setCreatedAt(new Date());
        } else {
            reaction = new GroupMessageReaction();
            reaction.setMessage(message);
            reaction.setUser(user);
            reaction.setReactionType(reactionType);
            reaction.setCreatedAt(new Date());
        }

        reactionRepository.save(reaction);
        return ResponseEntity.ok(Map.of(
                "messageId", messageId,
                "username", username,
                "reactionType", reactionType
        ));
    }

    // Remove a reaction
    @DeleteMapping("/groupMessageReaction/{username}/remove/reaction/{groupId}/{messageId}")
    public ResponseEntity<?> removeReaction(
            @PathVariable Long messageId,
            @PathVariable Long groupId,
            @PathVariable String username
    ) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is not a member of this group"));
        }

        GroupMessageReaction reaction = reactionRepository
                .findByMessageIdAndUserId(messageId, user.getId())
                .orElseThrow(() -> new RuntimeException("Reaction not found"));

        reactionRepository.delete(reaction);
        return ResponseEntity.ok(Map.of("message", "Reaction removed"));
    }

    // Get all reactions for a message (group-specific)
    @GetMapping("/groupMessageReaction/reactions/{groupId}/{messageId}")
    public ResponseEntity<List<Map<String, Object>>> getReactions(
            @PathVariable Long groupId,
            @PathVariable Long messageId
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        List<GroupMessageReaction> reactions = reactionRepository.findByMessageId(messageId);

        List<Map<String, Object>> response = reactions.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("username", r.getUser().getUsername());
            map.put("reactionType", r.getReactionType());
            map.put("createdAt", r.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get aggregated reaction counts for a message (group-specific)
    @GetMapping("/groupMessageReaction/summary/{groupId}/{messageId}")
    public ResponseEntity<Map<String, Object>> getReactionSummary(
            @PathVariable Long groupId,
            @PathVariable Long messageId
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        GroupMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        List<GroupMessageReaction> reactions = reactionRepository.findByMessageId(messageId);

        Map<String, Long> counts = reactions.stream()
                .collect(Collectors.groupingBy(GroupMessageReaction::getReactionType, Collectors.counting()));

        Map<String, Object> response = new HashMap<>();
        response.put("messageId", messageId);
        response.put("reactionCounts", counts);

        return ResponseEntity.ok(response);
    }
}
