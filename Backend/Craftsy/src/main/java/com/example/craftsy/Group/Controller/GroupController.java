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

    @PostMapping("/create/{adminUsername}")
    public ResponseEntity<Map<String, String>> createGroup(
            @PathVariable String adminUsername,
            @RequestBody Group groupRequest) {

        Optional<Users> adminOpt = userRepository.findByUsername(adminUsername);
        if (adminOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Admin not found"));
        }

        Users admin = adminOpt.get();

        if (groupRepository.findByGroupName(groupRequest.getGroupName()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group name already exists"));
        }

        Group group = new Group(); // create new group
        group.setGroupName(groupRequest.getGroupName());
        group.setGroupAdmin(admin); // <-- fixed
        group.setDescription(groupRequest.getDescription());
        group.setPrivate(groupRequest.isPrivate());
        group.setCraft(groupRequest.getCraft());
        group.setMembers(new HashSet<>());

        group.getMembers().add(admin); // add admin as first member
        groupRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Group created successfully"));
    }



    @PostMapping("/{groupName}/add-member/{username}")
    public ResponseEntity<Map<String, String>> addMember(
            @PathVariable String groupName,
            @PathVariable String username) {

        // 🔹 Decode spaces and special characters
        groupName = URLDecoder.decode(groupName, StandardCharsets.UTF_8);

        Optional<Group> groupOpt = groupRepository.findByGroupName(groupName);
        Optional<Users> userOpt = userRepository.findByUsername(username);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group or user not found"));
        }

        Group group = groupOpt.get();
        Users user = userOpt.get();

        if (group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already in group"));
        }

        group.getMembers().add(user);
        groupRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Member added successfully"));
    }




    @GetMapping("/{groupName}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable String groupName) {

        Optional<Group> groupOpt = groupRepository.findByGroupName(groupName);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group not found"));
        }

        Group group = groupOpt.get();

        List<Map<String, Object>> membersList = group.getMembers().stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("email", user.getEmail());
                    return map;
                })
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("groupName", group.getGroupName());
        response.put("totalMembers", membersList.size());
        response.put("members", membersList);

        return ResponseEntity.ok(response);
    }



    /**
     * DELETE /groups/{groupName}/remove-member/{username}
     * Remove a user from a group (admin only)
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

        if (!group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is not in this group"));
        }

        // Prevent removing admin accidentally
        if (group.getGroupAdmin().equals(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot remove group admin"));
        }

        group.getMembers().remove(user);
        groupRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }


}
