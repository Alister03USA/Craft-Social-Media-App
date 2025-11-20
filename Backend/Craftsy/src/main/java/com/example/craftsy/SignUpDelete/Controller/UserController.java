package com.example.craftsy.SignUpDelete.Controller;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Test: Create a new user account",
            description = "Registers a new Craftsy user. Password must contain uppercase, lowercase, number and be at least 8 characters long."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = Users.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
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
    @Operation(
            summary = "Delete a user by username",
            description = "Deletes a user from the database if the username exists."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
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
