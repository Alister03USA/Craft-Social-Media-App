package com.example.craftsy.feed;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class FeedController {

    @Autowired
    FeedRespository feedRespository;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/feed/{username}")
    Feed postProject(@PathVariable String username, @RequestBody Feed project){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        project.setUser(user);
        project.setDate(LocalDateTime.now());
        Feed proj = feedRespository.save(project);
        return proj;
    }
}
