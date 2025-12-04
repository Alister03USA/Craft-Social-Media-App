package com.example.craftsy.feed;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.PointsSystem.PointsService;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.feed.feedComments.FeedComments;
import com.example.craftsy.feed.feedComments.FeedCommentsRepository;
import com.example.craftsy.images.Image;
import com.example.craftsy.images.ImageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Autowired
    private PointsService pointsService;

    /**
     *
     * Posts a project to feed
     * @param username the user posting project
     * @param project the project being posted
     * @return the posted project
     */
    @Operation(
            summary = "Post a project",
            description = "Creates a new project for the given user, sets the date, links images, and awards points."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project posted",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/feed/{username}")
    Feed postProject(@Parameter(description = "The username of the user posting the project")
                     @PathVariable String username,
                     @Parameter(description = "The project content to post")
                     @RequestBody Feed project){
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

        // award 20 points after posting
        pointsService.awardPointsForPost(user, proj.getId());
        return proj;
    }

    /**
     * Get all projects from the user and the users they follow from most recent to oldest
     * @param username the user that is posting the project
     * @return list of projects
     */
    @Operation(
            summary = "Get a user's feed",
            description = "Returns all projects posted by the user and the users they follow, sorted from most recent to oldest."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed retrieved",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/feed/{username}")
    List<Feed> getUserFeed(@Parameter(description = "The username of the user whose feed is being retrieved")
                           @PathVariable String username){
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

    /**
     * gets all projects posted by one user
     * @param username
     * @return
     */
    @Operation(
            summary = "Get all projects by one user",
            description = "Returns every project posted by the specified user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects retrieved",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/feed/home/{username}")
    List<Feed> getUserProjects(@Parameter(description = "The username of the user whose projects are being retrieved")
                               @PathVariable String username){
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
     * gets one project post
     * @param username
     * @param projectName
     * @return
     */
    @Operation(
            summary = "Get a specific project",
            description = "Fetches a single project by username and project name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project retrieved",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User or project not found")
    })
    @GetMapping("/feed/{username}/{projectName}")
    Feed getProject(@Parameter(description = "The username of the project owner")
                    @PathVariable String username,
                    @Parameter(description = "The name of the project to retrieve")
                    @PathVariable String projectName){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = feedRepository.findByUserAndProjectName(user, projectName)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return project;
    }

    /**
     * Deletes the given project for given user
     * @param username user that has posted project
     * @param projectName name of project to be deleted
     * @return Post deleted, if successful; Post not found if post doesn't exist; User not found if user doesn't exist
     */
    @Operation(
            summary = "Delete a project",
            description = "Deletes a project for the given user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project deleted"),
            @ApiResponse(responseCode = "404", description = "User or project not found")
    })
    @DeleteMapping("/feed/{username}/{projectName}")
    String deleteProject(@Parameter(description = "The username of the project owner") @PathVariable String username,
                         @Parameter(description = "The name of the project to delete") @PathVariable String projectName){
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
    @Operation(
            summary = "Update a project",
            description = "Updates the details of a project for a given user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User or project not found")
    })
    @PutMapping("/feed/{username}/{projectName}")
    Feed updateProject(@Parameter(description = "The username of the project owner") @PathVariable String username,
                       @Parameter(description = "The name of the project to update") @PathVariable String projectName,
                       @Parameter(description = "The updated project details") @RequestBody Feed projectUpdated){
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

    @PutMapping("/feed/{username}/{projectName}/like")
    Feed likeProject(@PathVariable String username, @PathVariable String projectName){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = feedRepository.findByUserAndProjectName(user, projectName)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        project.setNumLikes(project.getNumLikes()+1);
        feedRepository.save(project);
        return project;
    }

    @PutMapping("/feed/{username}/{projectName}/unlike")
    Feed unlikeProject(@PathVariable String username, @PathVariable String projectName){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = feedRepository.findByUserAndProjectName(user, projectName)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if(project.getNumLikes() > 0){
            project.setNumLikes(project.getNumLikes()-1);
            feedRepository.save(project);
        }
        return project;
    }

    /**
     * adds a comment to a pattern
     * @param username user who posted pattern
     * @param projectName name of pattern
     * @param comment comment contents
     * @return the pattern with new comment
     */
    @Operation(
            summary = "Add a comment",
            description = "Adds a comment to a project and awards points."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment added",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User or project not found")
    })
    @PostMapping("/feed/{username}/{projectName}/comment/{commentUsername}")
    Feed addComment(@Parameter(description = "The username of the project owner") @PathVariable String username,
                    @Parameter(description = "The name of the project") @PathVariable String projectName,
                    @PathVariable String commentUsername,
                    @Parameter(description = "The comment to add") @RequestBody FeedComments comment){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = feedRepository.findByUserAndProjectName(user,projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Users commentUser = userRepository.findByUsername(commentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        comment.setUser(commentUser);
        comment.setDate(LocalDateTime.now());
        comment.setFeed(project);
        feedCommentsRepository.save(comment);

        // award 10 points after commenting
        pointsService.awardPointsForComment(user, comment.getId());
        return project;
    }

    /**
     * delete a comment from a post
     * @param username
     * @param projectName
     * @param id
     * @return pattern with updated comments
     */
    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment from a project."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment deleted",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User, project, or comment not found")
    })
    @DeleteMapping("/feed/{username}/{projectName}/{id}")
    Feed deleteComment(@Parameter(description = "The username of the project owner") @PathVariable String username,
                       @Parameter(description = "The name of the project") @PathVariable String projectName,
                       @Parameter(description = "The ID of the comment to delete") @PathVariable Long id){
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
    @Operation(
            summary = "Like a comment",
            description = "Adds a like to a comment for a given project."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment liked",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User, project, or comment not found")
    })
    @PutMapping("/feed/{username}/{projectName}/{id}")
    Feed likeComment(@Parameter(description = "The username of the project owner") @PathVariable String username,
                     @Parameter(description = "The name of the project") @PathVariable String projectName,
                     @Parameter(description = "The ID of the comment to like") @PathVariable Long id){
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

    /**
     * unlikes a comment
     * @param username
     * @param projectName
     * @param id
     * @return
     */
    @Operation(
            summary = "Unlike a comment",
            description = "Removes a like from a comment for a given project."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment unliked",
                    content = @Content(schema = @Schema(implementation = Feed.class))),
            @ApiResponse(responseCode = "404", description = "User, project, or comment not found")
    })
    @PutMapping("/feed/{username}/{projectName}/{id}/unlike")
    Feed unlikeComment(@Parameter(description = "The username of the project owner") @PathVariable String username,
                       @Parameter(description = "The name of the project") @PathVariable String projectName,
                       @Parameter(description = "The ID of the comment to unlike") @PathVariable Long id){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed feed = feedRepository.findByUserAndProjectName(user,projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        FeedComments comment = feedCommentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setLikes(comment.getLikes()-1);
        feedCommentsRepository.save(comment);
        return feed;
    }
}
