package com.example.craftsy.loginEditUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class LoginEditUserController {

    @Autowired
    LoginEditUserRepository loginEditUserRepository;

    /**
     * Updates the user information
     * @param username username of profile to update. Comes from path.
     * @param update The updated user information. Comes from body.
     * @return the updated user information
     */
    @Operation(
            summary = "Updates the user information",
            description = "Updates fields of a user's profile. Only the non-null fields in the request body will be updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully updated user information",
                    content = @Content(schema = @Schema(implementation = LoginEditUser.class))),
            @ApiResponse(responseCode = "400", description = "Password not strong enough"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/user/{username}")
    LoginEditUser editUser(
            @Parameter(description = "Username of profile to update", required = true) @PathVariable String username,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user information",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginEditUser.class))
            )@RequestBody LoginEditUser update){

        LoginEditUser user = loginEditUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(update.getUsername() != null) {
            user.setUsername(update.getUsername());
        }
        if(update.getBio() != null) {
            user.setBio(update.getBio());
        }
        if(update.getPassword() != null){
            if(!isPasswordStrong(update.getPassword())){
                throw new RuntimeException("Password not strong enough");
            }
            user.setPassword(update.getPassword());
        }
        if(update.getFollowers() != null){
            user.setFollowers(update.getFollowers());
        }
        if(update.getFollowing() != null){
            user.setFollowing(update.getFollowing());
        }
        if(update.getDisplayName() != null){
            user.setDisplayName(update.getDisplayName());
        }
        if(update.getEmail() != null){
            user.setEmail(update.getEmail());
        }
        if(update.getCraftSpecialties() != null){
            user.setCraftSpecialties(update.getCraftSpecialties());
        }
        if(update.getImage() != null){
            user.setImage(update.getImage());
        }
        loginEditUserRepository.save(user);
        return user;
    }

    /**
     * private class that confirms if password has at least 8 characters, one uppercase, and one number
     * @param password
     * @return
     */
    private boolean isPasswordStrong(String password) {
        // Example rules: at least 8 chars, one uppercase, one lowercase, one number
        String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        return password.matches(pattern);
    }

    /**
     * Logins user with username and password
     * @param username username for user
     * @param password password for user
     * @return User not found, {username} succesfully logged in, Incorrect password
     */
    @Operation(
            summary = "Login user with username and password",
            description = "Validates credentials and returns user information if successful."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully logged in",
                    content = @Content(schema = @Schema(implementation = LoginEditUser.class))),
            @ApiResponse(responseCode = "400", description = "Incorrect password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/login/{username}/{password}")
    LoginEditUser login(@Parameter(description = "Username of the user") @PathVariable String username,
                        @Parameter(description = "Password of the user") @PathVariable String password){
        LoginEditUser user = loginEditUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!user.getPassword().equals(password)) {
            throw new RuntimeException("Wrong password!");
        }
        return user;
    }
}
