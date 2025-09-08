package coms309.craftsy.controller;
import coms309.craftsy.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class CraftsyController {

    private Map<String, User> users = new HashMap<>();

    @PostMapping
    public User createUser(@RequestBody User user) { // RequestBody converts Json to user object
        users.put(user.getUsername(), user);
        return user;

    }

    // return the user information
    @GetMapping("/{username}")
    public User getUser(@PathVariable String username) {
        return users.get(username);
    }

    // Search by displayName
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String name) {
        return users.values().stream()
                .filter(u -> u.getDisplayName().toLowerCase().contains(name.toLowerCase()) ||
                        Arrays.stream(u.getCraftingSpecialties())
                                .anyMatch(s -> s.toLowerCase().contains(name.toLowerCase())))
                .collect(Collectors.toList());
    }

    // Replace the existing user with updated user info
    // (PathVariable - Extracts data from URL; RequestBody - Extracts data from http request
    @PutMapping("/update/{username}")
    public User updateUser(@PathVariable String username, @RequestBody User user) {
        users.put(username, user);
        return user;
    }


    // Delete User
    @DeleteMapping("/{username}")
    public String deleteUser(@PathVariable String username) {
        users.remove(username);
        return username + " has been deleted!";
    }

    // List all users
    @GetMapping
    public Collection<User> listOfUsers(){
        return users.values();
    }


    // Test exceptions
    @GetMapping("/oops")
    public String oops() {
        throw new RuntimeException("Something went wrong!");
    }





}
