package com.example.craftsy.loginEditUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class LoginEditUserController {

    @Autowired
    LoginEditUserRepository loginEditUserRepository;

    @PutMapping("/user/{username}")
    LoginEditUser editUser(@PathVariable String username, @RequestBody LoginEditUser update){
        LoginEditUser user = loginEditUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(!update.getUsername().isEmpty()) {
            user.setUsername(update.getUsername());
        }
        if(!update.getBio().isEmpty()) {
            user.setBio(update.getBio());
        }
        if(!update.getCraftSpecialties().isEmpty()) {
            user.setCraftSpecialties(update.getCraftSpecialties());
        }
        if(!update.getEmail().isEmpty()) {
            user.setEmail(update.getEmail());
        }
        if(!update.getDisplayName().isEmpty()) {
            user.setDisplayName(update.getDisplayName());
        }
        if(!update.getPassword().isEmpty()) {
            user.setPassword(update.getPassword());
        }
        user.setFollowers(update.getFollowers());
        user.setFollowing(update.getFollowing());

        return user;
    }

    @GetMapping("/login")
    String login(@RequestBody String username, @RequestBody String password){
        LoginEditUser user = loginEditUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(user.getPassword().equals(password)){
            return username + " successfully logged in.";
        }
        else{
            return "Incorrect password";
        }
    }
}
