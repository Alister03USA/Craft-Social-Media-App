package com.example.craftsy.SignUpDelete.Controller;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.util.Optional;

@RestController
@RequestMapping("/users") //
public class UserController {

    // Inject the repo automatically
    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    // Map the json user body to a User object
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Users user) {
        // Check password strength
        if (!isPasswordStrong(user.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body("Password too weak! Must be at least 8 characters, contain uppercase, lowercase, and a number.");
        }

    /*    // Hash password before saving
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);*/


        Users savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    private boolean isPasswordStrong(String password) {
        // Example rules: at least 8 chars, one uppercase, one lowercase, one number
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(pattern);
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) { // Extracts username from the URL
        // Optional: Java class - Hold a value or be Null
        Optional<Users> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            userRepository.delete(user.get()); // get user object
            return ResponseEntity.ok("User " + username + " deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}