package com.example.craftsy.loginEditUser;

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
    @PutMapping("/user/{username}")
    LoginEditUser editUser(@PathVariable String username, @RequestBody LoginEditUser update){
        //creates user from body. Throws exception if username not found.
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
    @GetMapping("/login/{username}/{password}")
    LoginEditUser login(@PathVariable String username, @PathVariable String password){
        LoginEditUser user = loginEditUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!user.getPassword().equals(password)) {
            throw new RuntimeException("Wrong password!");
        }
        return user;
    }
}
