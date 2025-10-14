package com.example.craftsy.Search.Controller;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;


import com.example.craftsy.feed.Feed;
import com.example.craftsy.feed.FeedRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FeedRespository feedRespository;


    /**
     * GET /search/users?query=abc
     * Search for users by username
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String query
                                                               ) { // Look for a parameter and pass its value to the method
        List<Users> users = userRepository.findByUsernameContainingIgnoreCase(query);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Users u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("displayName", u.getDisplayName());
            map.put("bio", u.getBio());
            map.put("followers", u.getFollowers());
            map.put("following", u.getFollowing());
            map.put("craftSpecialties", u.getCraftSpecialties());
//            map.put("isFollowing", followRepository.existsByFollowerAndFollowing(currentUser, u));
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /search/groups?query=abc
     * Search for groups by group name
     */
    @GetMapping("/groups")
    public ResponseEntity<List<Map<String, Object>>> searchGroups(@RequestParam String query) {
        List<Group> groups = groupRepository.findByGroupNameContainingIgnoreCase(query);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Group g : groups) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", g.getId());
            map.put("groupName", g.getGroupName());
            map.put("description", g.getDescription());
            map.put("memberCount", g.getMemberCount());
            map.put("isPrivate", g.isPrivate());
            map.put("craft", g.getCraft());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }


    /**
     * GET /search/projects?query=abc
     * Search for projects by projectName
     */
    @GetMapping("/projects")
    public ResponseEntity<List<Map<String, Object>>> searchProjects(@RequestParam String query) {
        List<Feed> projects = feedRespository.findByProjectNameContainingIgnoreCase(query);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Feed p : projects) {
            Map<String, Object> map = new HashMap<>();
            map.put("User", p.getUser());
            map.put("projectName", p.getProjectName());
            map.put("description", p.getProjectDesc());
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }



}
