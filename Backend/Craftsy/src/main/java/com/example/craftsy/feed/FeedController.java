package com.example.craftsy.feed;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
public class FeedController {

//    @Autowired
//    FollowRepository followRepository;
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

    @GetMapping("/feed/{username}")
    List<Feed> getUserFeed(@PathVariable String username){
        List<Feed> userFeed = new ArrayList<Feed>();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<List<Feed>> optFeed = feedRespository.findByUser(user);
        if(optFeed.isPresent()){
            userFeed = optFeed.orElseThrow(() -> new RuntimeException("User not found"));
        }
//        List<Follow> following = followRepository.findByFollower(user);
//        for(int i=0; i<following.size(); i++){
//            userFeed.addAll(feedRespository.findByUser(following.get(i).following));
//        }
        userFeed.sort(Comparator.comparing(Feed::getDate).reversed());
        return userFeed;
    }

    @DeleteMapping("/feed/{username}/{projectName}")
    String deleteProject(@PathVariable String username, @PathVariable String projectName){
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if(userOpt.isEmpty()){
            return "User not found";
        }
        Users user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
        Optional<Feed> projectOpt = feedRespository.findByUserAndProjectName(user, projectName);
        if(projectOpt.isEmpty()){
            return "Post not found";
        }
        Feed project = projectOpt.orElseThrow(() -> new RuntimeException("Post not found"));
        feedRespository.delete(project);
        return "Post deleted";
    }

    @PutMapping("/feed/{username}/{projectName}")
    Feed updateProject(@PathVariable String username, @PathVariable String projectName,
                       @RequestBody Feed projectUpdated){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = feedRespository.findByUserAndProjectName(user, projectName)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if(projectUpdated.getProjectName() != null){
            project.setProjectName(projectUpdated.getProjectName());
        }
        if(projectUpdated.getProjectDesc() != null){
            project.setProjectDesc(projectUpdated.getProjectDesc());
        }
        if(projectUpdated.getProjectPic() != null){
            project.setProjectPic(projectUpdated.getProjectPic());
        }
        if(projectUpdated.getProjectType() != null){
            project.setProjectType(projectUpdated.getProjectType());
        }
        if(projectUpdated.getSupplies() != null){
            project.setSupplies(projectUpdated.getSupplies());
        }
        if(projectUpdated.getVisibility() != null){
            project.setVisibility(projectUpdated.getVisibility());
        }
        feedRespository.save(project);
        return project;
    }
}
