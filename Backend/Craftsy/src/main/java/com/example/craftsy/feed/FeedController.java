package com.example.craftsy.feed;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.feed.feedComments.FeedComments;
import com.example.craftsy.feed.feedComments.FeedCommentsRepository;
import com.example.craftsy.images.Image;
import com.example.craftsy.images.ImageRepository;
import com.example.craftsy.patterns.Patterns;
import com.example.craftsy.patterns.patternsComments.PatternsComments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
public class FeedController {

    @Autowired
    FollowRepository followRepository;
    @Autowired
    FeedRepository feedRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FeedCommentsRepository feedCommentsRepository;
    @Autowired
    ImageRepository imageRepository;

    /**
     *
     * Posts a project to feed
     * @param username the user posting project
     * @param project the project being posted
     * @return the posted project
     */
    @PostMapping("/feed/{username}")
    Feed postProject(@PathVariable String username, @RequestBody Feed project){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        project.setUser(user);
        project.setDate(LocalDateTime.now());

        if (project.getImages() != null && !project.getImages().isEmpty()) {
            List<Long> imageIds = project.getImages().stream()
                    .map(Image::getId)
                    .toList();
            List<Image> existingImages = imageRepository.findAllById(imageIds);
            project.setImages(existingImages);
        }

        Feed proj = feedRepository.save(project);
        return proj;
    }

    /**
     * Get all projects from the user and the users they follow from most recent to oldest
     * @param username the user that is posting the project
     * @return list of projects
     */
    @GetMapping("/feed/{username}")
    List<Feed> getUserFeed(@PathVariable String username){
        List<Feed> userFeed = new ArrayList<Feed>();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<List<Feed>> optFeed = feedRepository.findByUser(user);
        if(optFeed.isPresent()){
            userFeed = optFeed.orElseThrow(() -> new RuntimeException("User not found"));
        }
        List<Follow> following = followRepository.findByFollower(user);
        for(int i=0; i<following.size(); i++){
            List<Feed> userFollowing = feedRepository.findByUser(following.get(i).getFollowing())
                            .orElseThrow(()-> new RuntimeException("Following not found"));
            userFeed.addAll(userFollowing);
        }
        userFeed.sort(Comparator.comparing(Feed::getDate).reversed());
        return userFeed;
    }

    @GetMapping("/feed/home/{username}")
    List<Feed> getUserProjects(@PathVariable String username){
        List<Feed> userFeed = new ArrayList<Feed>();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<List<Feed>> optFeed = feedRepository.findByUser(user);
        if(optFeed.isPresent()){
            userFeed = optFeed.orElseThrow(() -> new RuntimeException("User not found"));
        }
        return userFeed;
    }

    /**
     * Deletes the given project for given user
     * @param username user that has posted project
     * @param projectName name of project to be deleted
     * @return Post deleted, if successful; Post not found if post doesn't exist; User not found if user doesn't exist
     */
    @DeleteMapping("/feed/{username}/{projectName}")
    String deleteProject(@PathVariable String username, @PathVariable String projectName){
        Optional<Users> userOpt = userRepository.findByUsername(username);
        if(userOpt.isEmpty()){
            return "User not found";
        }
        Users user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));
        Optional<Feed> projectOpt = feedRepository.findByUserAndProjectName(user, projectName);
        if(projectOpt.isEmpty()){
            return "Post not found";
        }
        Feed project = projectOpt.orElseThrow(() -> new RuntimeException("Post not found"));
        feedRepository.delete(project);
        return "Post deleted";
    }

    /**
     * Updates project with given information
     * @param username the user that posted the project
     * @param projectName the name of the project to be updated
     * @param projectUpdated the updated information
     * @return the updated project
     */
    @PutMapping("/feed/{username}/{projectName}")
    Feed updateProject(@PathVariable String username, @PathVariable String projectName,
                       @RequestBody Feed projectUpdated){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = feedRepository.findByUserAndProjectName(user, projectName)
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
        if (project.getImages() != null && !project.getImages().isEmpty()) {
            List<Long> imageIds = project.getImages().stream()
                    .map(Image::getId)
                    .toList();
            List<Image> existingImages = imageRepository.findAllById(imageIds);
            project.setImages(existingImages);
        }
        feedRepository.save(project);
        return project;
    }

    /**
     * adds a comment to a pattern
     * @param username user who posted pattern
     * @param projectName name of pattern
     * @param comment comment contents
     * @return the pattern with new comment
     */
    @PostMapping("/feed/{username}/{projectName}/comment")
    Feed addComment(@PathVariable String username, @PathVariable String projectName,
                        @RequestBody FeedComments comment){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = feedRepository.findByUserAndProjectName(user,projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        comment.setDate(LocalDateTime.now());
        comment.setFeed(project);
        feedCommentsRepository.save(comment);
        return project;
    }

    /**
     * delete a comment from a post
     * @param username
     * @param projectName
     * @param id
     * @return pattern with updated comments
     */
    @DeleteMapping("/feed/{username}/{projectName}/{id}")
    Feed deleteComment(@PathVariable String username, @PathVariable String projectName,
                           @PathVariable Long id){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = feedRepository.findByUserAndProjectName(user,projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        FeedComments comment = feedCommentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        feedCommentsRepository.delete(comment);
        return project;
    }

    /**
     * like a comment
     * @param username
     * @param projectName
     * @param id
     * @return updated pattern contents
     */
    @PutMapping("/feed/{username}/{projectName}/{id}")
    Feed likeComment(@PathVariable String username, @PathVariable String projectName,
                         @PathVariable Long id){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed feed = feedRepository.findByUserAndProjectName(user,projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        FeedComments comment = feedCommentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setLikes(comment.getLikes()+1);
        feedCommentsRepository.save(comment);
        return feed;
    }
}
