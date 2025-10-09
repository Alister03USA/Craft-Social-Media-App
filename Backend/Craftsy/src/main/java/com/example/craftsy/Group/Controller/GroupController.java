package com.example.craftsy.Group.Controller;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * POST /groups/create/{adminUsername}
     * This endpoint lets a user create a new group.
     * The user who creates the group becomes the admin automatically.
     */
    @PostMapping("/create/{adminUsername}")
    public ResponseEntity<Map<String, String>> createGroup(
            @PathVariable String adminUsername,
            @RequestBody Group groupRequest) {

        // Find the user who wants to be the admin
        Optional<Users> adminOpt = userRepository.findByUsername(adminUsername);
        if (adminOpt.isEmpty()) {
            // If the user doesn't exist, we can't create the group
            return ResponseEntity.badRequest().body(Map.of("message", "Admin not found"));
        }

        Users admin = adminOpt.get();

        // Check if a group with the same name already exists
        if (groupRepository.findByGroupName(groupRequest.getGroupName()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group name already exists"));
        }

        // Create the new group object
        Group group = new Group();
        group.setGroupName(groupRequest.getGroupName());
        group.setGroupAdmin(admin); // Admin is the creator
        group.setDescription(groupRequest.getDescription());
        group.setPrivate(groupRequest.isPrivate());
        group.setCraft(groupRequest.getCraft());
        group.setMembers(new HashSet<>());

        // Add the admin as the first member
        group.getMembers().add(admin);

        // Save the group to the database
        groupRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Group created successfully"));
    }

    /**
     * POST /groups/{groupName}/add-member/{username}
     * Add a new user to an existing group.
     */
    @PostMapping("/{groupName}/add-member/{username}")
    public ResponseEntity<Map<String, String>> addMember(
            @PathVariable String groupName,
            @PathVariable String username) {

        // Decode any spaces or special characters in the group name
        groupName = URLDecoder.decode(groupName, StandardCharsets.UTF_8);

        // Look up the group and user in the database
        Optional<Group> groupOpt = groupRepository.findByGroupName(groupName);
        Optional<Users> userOpt = userRepository.findByUsername(username);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group or user not found"));
        }

        Group group = groupOpt.get();
        Users user = userOpt.get();

        // Prevent adding the same user twice
        if (group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already in group"));
        }

        // Add the user to the group
        group.getMembers().add(user);
        groupRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Member added successfully"));
    }

    /**
     * GET /groups/{groupName}/members
     * Retrieve all members of a group.
     */
    @GetMapping("/{groupName}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable String groupName) {

        Optional<Group> groupOpt = groupRepository.findByGroupName(groupName);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group not found"));
        }

        Group group = groupOpt.get();

        // Build a list of members with their basic info
        List<Map<String, Object>> membersList = new ArrayList<>();
        for (Users user : group.getMembers()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            membersList.add(map);
        }

        // Build the response
        Map<String, Object> response = new HashMap<>();
        response.put("groupName", group.getGroupName());
        response.put("totalMembers", membersList.size());
        response.put("members", membersList);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /groups/{groupName}/remove-member/{username}
     * Remove a user from a group. Only admins can do this.
     */
    @DeleteMapping("/{groupName}/remove-member/{username}")
    public ResponseEntity<Map<String, String>> removeMember(
            @PathVariable String groupName,
            @PathVariable String username) {

        Optional<Group> groupOpt = groupRepository.findByGroupName(groupName);
        Optional<Users> userOpt = userRepository.findByUsername(username);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group or user not found"));
        }

        Group group = groupOpt.get();
        Users user = userOpt.get();

        // Check if the user is actually in the group
        if (!group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is not in this group"));
        }

        // Prevent removing the group admin
        if (group.getGroupAdmin().equals(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot remove group admin"));
        }

        // Remove the user and save the group
        group.getMembers().remove(user);
        groupRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }
}
