package com.example.craftsy.Search.Controller;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.Group.Repository.GroupRepository;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;


import com.example.craftsy.Tutorial.Entity.Tutorial;
import com.example.craftsy.Tutorial.Repository.TutorialRepository;
import com.example.craftsy.feed.Feed;
import com.example.craftsy.feed.FeedRepository;
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
    private FeedRepository feedRespository;

    @Autowired
    private TutorialRepository tutorialRepository;


    /**
     * GET /search/user?query=abc
     * Search for users by username
     */
    @GetMapping("/user")
    public ResponseEntity<List<Map<String, Object>>> searchUsers(@RequestParam String query,
                                                                 @RequestParam(required = false) String viewerUsername // who is searching
                                                               ) { // Look for a parameter and pass its value to the method
        List<Users> users = userRepository.findByUsernameContainingIgnoreCase(query);
        List<Map<String, Object>> result = new ArrayList<>();

        // If viewer exists, fetch their info once
        Optional<Users> viewerOpt = viewerUsername != null ? userRepository.findByUsername(viewerUsername) : Optional.empty();
        Users viewer = viewerOpt.orElse(null);

        for (Users u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("displayName", u.getDisplayName());
            map.put("bio", u.getBio());
            map.put("followers", u.getFollowers());
            map.put("following", u.getFollowing());
            map.put("craftSpecialties", u.getCraftSpecialties());


            // Relationship status
            boolean isFollowing = false;
            boolean isPending = false;

            if (viewer != null && !viewer.getUsername().equals(u.getUsername())) {
                Optional<Follow> followOpt = followRepository.findByFollowerAndFollowing(viewer, u);
                if (followOpt.isPresent()) {
                    Follow f = followOpt.get();
                    if (f.isAccepted()) {
                        isFollowing = true;
                    } else {
                        isPending = true;
                    }
                }
            }

            map.put("isFollowing", isFollowing);
            map.put("isPending", isPending);
            result.add(map);
        }


        return ResponseEntity.ok(result);
    }

    /**
     * GET /search/group?query=abc
     * Search for groups by group name or username
     */
    @GetMapping("/group")
    public ResponseEntity<List<Map<String, Object>>> searchGroups(@RequestParam String query) {
        List<Group> groups = groupRepository.findByGroupNameContainingIgnoreCaseOrGroupAdmin_UsernameContainingIgnoreCase(query, query);
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
     * GET /search/project?query=abc
     * Search for projects by projectName or Username to find the user's project
     */
    @GetMapping("/project")
    public ResponseEntity<List<Map<String, Object>>> searchProjects(@RequestParam String query) {
        List<Feed> projects = feedRespository.findByProjectNameContainingIgnoreCaseOrUser_UsernameContainingIgnoreCase(query, query);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Feed p : projects) {
            Map<String, Object> map = new HashMap<>();
            map.put("username", p.getUser().getUsername());
            map.put("projectName", p.getProjectName());
            map.put("projectType", p.getProjectType());
            map.put("supplies", p.getSupplies());
            map.put("projectDesc", p.getProjectDesc());
            map.put("projectPic", p.getProjectPic());
            map.put("visibility", p.getVisibility());
            map.put("date", p.getDate());
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Search for tutorial by username, title, description, or category
     */
    @GetMapping("/tutorial")
    public ResponseEntity<List<Map<String, Object>>> searchTutorials(@RequestParam String query) {
        List<Tutorial> tutorials = tutorialRepository
                .findByUser_UsernameContainingIgnoreCaseOrTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        query, query, query, query);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Tutorial t : tutorials) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("username", t.getUser());
            map.put("title", t.getTitle());
            map.put("description", t.getDescription());
            map.put("category", t.getCategory());
            map.put("fileUrl", t.getFileUrl());
            map.put("filePath", t.getFilePath());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }



}
