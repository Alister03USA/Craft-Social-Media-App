package com.example.craftsy.SignUpDelete.Controller;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users") // sets the base path for all endpoints in this controller.
public class UserController {

    // Injects an instance of UserRepository so you can use it without manually creating it.
    @Autowired
    private UserRepository userRepository;

    // maps the HTTP POST request to the signup
    @PostMapping("/signup")
    public ResponseEntity<Users> signup(@RequestBody Users user) { // Converts incoming JSON into java object
        Users savedUser = userRepository.save(user); // java -> SQL
        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        Optional<Users> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            userRepository.delete(user.get()); // uses entity, transaction handled automatically
            return ResponseEntity.ok("User " + username + " deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}