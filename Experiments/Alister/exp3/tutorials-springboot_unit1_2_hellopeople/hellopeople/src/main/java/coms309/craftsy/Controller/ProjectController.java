package coms309.craftsy.Controller;

import coms309.craftsy.Model.Project;
import coms309.craftsy.Model.User;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private Map<String, Project> projects = new HashMap<>();
    private final UserController userController;

    public ProjectController(UserController userController) {
        this.userController = userController;
    }

    // CREATE a project
    @PostMapping
    public Project createProject(@RequestBody Project project) {
        projects.put(project.getId(), project);
        return project;
    }

    // GET feed for a user (projects from followed users)
    @GetMapping("/feed/{userName}")
    public List<Project> getFeed(@PathVariable String userName) {
        Map<String, User> users = userController.listUsers().stream()
                .collect(Collectors.toMap(User::getUserName, u -> u));

        User user = users.get(userName);
        if(user == null) return new ArrayList<>();

        List<Project> feed = new ArrayList<>();
        for(Project p : projects.values()){
            if(user.getFollowing().contains(p.getCreatorUsername())){
                feed.add(p);
            }
        }
        return feed;
    }
}
