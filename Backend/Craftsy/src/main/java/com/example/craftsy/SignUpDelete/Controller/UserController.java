package com.example.craftsy.SignUpDelete.Controller;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController                           // Marks this class as a REST controller (handles HTTP requests)
@RequestMapping("/users")                 // All routes here will start with "/users"
public class UserController {

    // Injects the UserRepository automatically
    @Autowired
    private UserRepository userRepository;



    /**
     * POST /users/signup
     * Creates a new user account.
     * Validates password strength, and if valid, saves the user to the database.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Users user) {
        //  Check password strength using regex pattern
        if (!isPasswordStrong(user.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body("Password too weak! Must be at least 8 characters, contain uppercase, lowercase, and a number.");
        }


        //  Save user details in the database using JPA
        Users savedUser = userRepository.save(user);

        //  Return success response with created user data
        return ResponseEntity.ok(savedUser);
    }


    /**
     * Helper function — checks if a password meets the required strength.
     * Must have:
     *  - At least 8 characters
     *  - One uppercase letter
     *  - One lowercase letter
     *  - One number
     */
    private boolean isPasswordStrong(String password) {
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(pattern);
    }


    /**
     * DELETE /users/delete/{username}
     * Deletes a user account by username.
     * Uses Optional to safely handle cases where the user might not exist.
     */
    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        // Try to find the user in the database
        Optional<Users> user = userRepository.findByUsername(username);

        // If found, delete from DB
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return ResponseEntity.ok("User " + username + " deleted successfully");
        }
        // If not found  = return 404
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
