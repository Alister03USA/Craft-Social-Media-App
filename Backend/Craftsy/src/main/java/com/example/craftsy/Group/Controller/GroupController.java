package com.example.craftsy.Group.Controller;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Entity.GroupJoinRequest;
import com.example.craftsy.Group.Repository.GroupJoinRequestRepository;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.Notification.Entity.Notification;
import com.example.craftsy.Notification.NotificationWebSocket;
import com.example.craftsy.Notification.Repository.NotificationRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupJoinRequestRepository groupJoinRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;


    /**
     * POST /groups/create/{adminUsername}
     * This endpoint lets a user create a new group.
     * The user who creates the group becomes the admin automatically.
     */
    @PostMapping("/create/{adminUsername}")
    public ResponseEntity<Map<String, String>> createGroup(
            @PathVariable String adminUsername, // takes value from url path to the parameter
            @RequestBody Group groupRequest) { // convert the json body from client to Group object

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
     * POST /{groupName}/{admin}/add-member/{username}
     * @param groupName
     * @param username
     * @param admin
     * @return
     */
    @PostMapping("/{groupName}/{admin}/add-member/{username}")
    public ResponseEntity<Map<String, String>> addMember(
            @PathVariable String groupName,
            @PathVariable String username,
            @PathVariable String admin) {

        // Decode the group name
        groupName = URLDecoder.decode(groupName, StandardCharsets.UTF_8);

        Optional<Group> groupOpt = groupRepository.findByGroupName(groupName);
        Optional<Users> userOpt = userRepository.findByUsername(username);
        Optional<Users> adminOpt = userRepository.findByUsername(admin);

        if (groupOpt.isEmpty() || userOpt.isEmpty() || adminOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group or user not found"));
        }

        Group group = groupOpt.get();
        Users user = userOpt.get();
        Users currentUser = adminOpt.get();

        // Only admin can add members
        if (!group.getGroupAdmin().equals(currentUser)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only the group admin can add members"));
        }

        // Prevent adding duplicate members
        if (group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already in group"));
        }

        group.getMembers().add(user);
        groupRepository.save(group);

        return ResponseEntity.ok(Map.of("message", "Member added successfully"));
    }


    /**
     * GET /{groupName}/members
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
     * POST  /{username}/join/{group_name}
     * Public Group - Join directly
     * Private Group - Sent request (only can be accepted/Declined by Admin)
     */
    @PostMapping("/{username}/join/{groupName}")
    public ResponseEntity<Map<String, String>> joinGroup(
            @PathVariable String username,
            @PathVariable String groupName) {

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

        // public group: add immediately
        if (!group.isPrivate()) {
            // Public group: add user immediately
            group.getMembers().add(user);
            group.setMemberCount();
            groupRepository.save(group);

            // Notify all members
            for (Users member : group.getMembers()) {
                if (member.equals(user)) continue; // optional: skip self

                Notification notif = new Notification();
                notif.setUser(member);
                notif.setTitle("New Member Joined");
                notif.setMessage(user.getUsername() + " has joined " + group.getGroupName());
                notif.setCreatedAt(new Date());
                notif.setIsRead(false);

                notificationRepository.save(notif);
                NotificationWebSocket.pushNotification(member.getUsername(), notif);
            }

            return ResponseEntity.ok(Map.of("message", "User added to the group successfully"));

        } else {
            // Private group: create join request
            if (groupJoinRequestRepository.findByGroupAndUser(group, user).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Request already sent"));
            }

            GroupJoinRequest groupJoinRequest = new GroupJoinRequest();
            groupJoinRequest.setGroup(group);
            groupJoinRequest.setUser(user);
            groupJoinRequestRepository.save(groupJoinRequest);

            Notification notif = new Notification();
            notif.setUser(group.getGroupAdmin());
            notif.setTitle("New Join Request");
            notif.setMessage(user.getUsername() + " wants to join " + group.getGroupName());
            notif.setReferenceId(groupJoinRequest.getId());
            notif.setCreatedAt(new Date());
            notif.setIsRead(false);

            notificationRepository.save(notif);
            NotificationWebSocket.pushNotification(group.getGroupAdmin().getUsername(), notif);

            return ResponseEntity.ok(Map.of("message", "Join request sent successfully"));
        }
        }




    /**
     * PUT - /joinRequest/{requestId}/{accepted}
     * Private Group only - Admin to accept or decline the join request
     */
    @PutMapping("/joinRequest/{requestId}/{accepted}")
    public ResponseEntity<Map<String, String>> handleJoinRequest(
            @PathVariable Long requestId,
            @PathVariable boolean accepted) {

        Optional<GroupJoinRequest> requestOpt = groupJoinRequestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Join request not found"));
        }

        GroupJoinRequest request = requestOpt.get();
        Group group = request.getGroup();
        Users user = request.getUser();

        if (accepted) {
            group.getMembers().add(user);
            group.setMemberCount();
            groupRepository.save(group);
            request.setAccepted(true);
            groupJoinRequestRepository.save(request);

            // Notify the new user
            Notification userNotif = new Notification();
            userNotif.setUser(user);
            userNotif.setTitle("Join Request Accepted");
            userNotif.setMessage("You have been added to group: " + group.getGroupName());
            userNotif.setReferenceId(group.getId());
            userNotif.setCreatedAt(new Date());
            userNotif.setIsRead(false);
            notificationRepository.save(userNotif);
            NotificationWebSocket.pushNotification(user.getUsername(), userNotif);

            // Notify all other group members
            for (Users member : group.getMembers()) {
                if (member.equals(user)) continue; // skip the new user

                Notification notif = new Notification();
                notif.setUser(member);
                notif.setTitle("New Member Joined");
                notif.setMessage(user.getUsername() + " has joined " + group.getGroupName());
                notif.setCreatedAt(new Date());
                notif.setIsRead(false);
                notificationRepository.save(notif);
                NotificationWebSocket.pushNotification(member.getUsername(), notif);
            }

            return ResponseEntity.ok(Map.of("message", "User added to group"));
        } else {
            groupJoinRequestRepository.delete(request);

            Notification notif = new Notification();
            notif.setUser(user);
            notif.setTitle("Join Request Declined");
            notif.setMessage("Your request to join " + group.getGroupName() + " was declined");
            notif.setReferenceId(group.getId());
            notif.setCreatedAt(new Date());
            notif.setIsRead(false);

            notificationRepository.save(notif);
            NotificationWebSocket.pushNotification(user.getUsername(), notif);

            return ResponseEntity.ok(Map.of("message", "Join request declined"));
        }
    }


    /**
     * GET /user/{username}
     * Retrieve all groups that a user is a member of.
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserGroups(@PathVariable String username) {
        // Find the user
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        Users user = userOpt.get();

        // Find all groups the user is a member of
        List<Group> userGroups = groupRepository.findAll()
                .stream()
                .filter(g -> g.getMembers().contains(user))
                .toList();

        List<Map<String, Object>> groupList = new ArrayList<>();
        for (Group group : userGroups) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", group.getId());
            map.put("groupName", group.getGroupName());
            map.put("description", group.getDescription());
            map.put("isPrivate", group.isPrivate());
            map.put("craft", group.getCraft());
            map.put("memberCount", group.getMembers().size());
            map.put("admin", group.getGroupAdmin().getUsername());
            groupList.add(map);
        }

        return ResponseEntity.ok(Map.of(
                "username", username,
                "totalGroups", groupList.size(),
                "groups", groupList
        ));
    }

    /**
     * DELETE /{username}/leave/{groupName}
     * Allows a member to leave a group voluntarily.
     */
    @DeleteMapping("/{username}/leave/{groupName}")
    public ResponseEntity<Map<String, String>> leaveGroup(
            @PathVariable String groupName,
            @PathVariable String username) {

        // Decode group name if it has spaces or special characters
        groupName = URLDecoder.decode(groupName, StandardCharsets.UTF_8);

        Optional<Group> groupOpt = groupRepository.findByGroupName(groupName);
        Optional<Users> userOpt = userRepository.findByUsername(username);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group or user not found"));
        }

        Group group = groupOpt.get();
        Users user = userOpt.get();

        // Check if user is actually a member
        if (!group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User is not a member of this group"));
        }

        // Prevent the admin from leaving their own group
        if (group.getGroupAdmin().equals(user)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Admin cannot leave their own group. Transfer admin role or delete the group."));
        }

        // Remove the user from the group
        group.getMembers().remove(user);
        group.setMemberCount();
        groupRepository.save(group);

        // Notify all remaining members
        for (Users member : group.getMembers()) {
            Notification notif = new Notification();
            notif.setUser(member);
            notif.setTitle("Member Left");
            notif.setReferenceId(group.getId());
            notif.setMessage(user.getUsername() + " has left " + group.getGroupName());
            notif.setCreatedAt(new Date());
            notif.setIsRead(false);

            notificationRepository.save(notif);
            NotificationWebSocket.pushNotification(member.getUsername(), notif);
        }



        return ResponseEntity.ok(Map.of("message", "You have left the group successfully"));
    }




    /**
     * DELETE /groups/{groupName}/remove-member/{username}
     * Remove a user from a group. Only admins can do this.
     */
    @DeleteMapping("/{groupName}/{admin}/removeMember/{username}")
    public ResponseEntity<Map<String, String>> removeMember(
            @PathVariable String groupName,
            @PathVariable String username,
            @PathVariable String admin) {

        Optional<Group> groupOpt = groupRepository.findByGroupName(groupName);
        Optional<Users> userOpt = userRepository.findByUsername(username);

        if (groupOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Group or user not found"));
        }

        Group group = groupOpt.get();
        Users user = userOpt.get();

        // Check admin
        if (!group.getGroupAdmin().equals(admin)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only the group admin can remove members"));

        }

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
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setTitle("Removed from Group");
        notif.setMessage("You have been removed from " + group.getGroupName());
        notif.setCreatedAt(new Date());
        notif.setIsRead(false);

        notificationRepository.save(notif);
        NotificationWebSocket.pushNotification(user.getUsername(), notif);

        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }
}
