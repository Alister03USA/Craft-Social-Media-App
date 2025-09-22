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
        user.setBio(update.getBio());
        user.setCraftSpecialties(update.getCraftSpecialties());
        user.setEmail(update.getEmail());
        user.setDisplayName(update.getDisplayName());
        user.setPassword(update.getPassword());
        user.setFollowers(update.getFollowers());
        user.setFollowing(update.getFollowing());

        return user;
    }

    @GetMapping("/login")
    String login(@RequestBody LoginEditUser userInfo){
        if(loginEditUserRepository.findByUsername(userInfo.getUsername()).isEmpty()){
            return "User not found";
        }
        LoginEditUser user = loginEditUserRepository.findByUsername(userInfo.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(user.getPassword().equals(userInfo.getPassword())){
            return userInfo.getUsername() + " successfully logged in.";
        }
        else{
            return "Incorrect password";
        }
    }
}
