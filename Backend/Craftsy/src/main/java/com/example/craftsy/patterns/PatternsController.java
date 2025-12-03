package com.example.craftsy.patterns;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.images.Image;
import com.example.craftsy.images.ImageRepository;
import com.example.craftsy.patterns.patternsComments.PatternsComments;
import com.example.craftsy.patterns.patternsComments.PatternsCommentsRepository;
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
import java.util.regex.Pattern;

@RestController
public class PatternsController {

    @Autowired
    PatternsRepository patternsRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PatternsCommentsRepository patternsCommentsRepository;
    @Autowired
    FollowRepository followRepository;
    @Autowired
    ImageRepository imageRepository;

    /**
     * Posts a pattern from username
     * @param pattern the pattern to be posted
     * @param username the user that is posting the pattern
     * @return The posted pattern
     */
    @Operation(
            summary = "Posts a new pattern",
            description = "Creates a new pattern associated with the given username. "
                    + "Images will be linked by ID if provided."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern posted successfully",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/patterns/{username}")
    Patterns postPattern(@io.swagger.v3.oas.annotations.parameters.RequestBody(
                                 description = "Pattern data to be created",
                                 required = true,
                                 content = @Content(schema = @Schema(implementation = Patterns.class))
                         )
                         @RequestBody Patterns pattern,
                         @Parameter(description = "Username of the posting user", required = true)
                         @PathVariable String username){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        pattern.setUser(user);
        pattern.setDate(LocalDateTime.now());

        if (pattern.getImages() != null && !pattern.getImages().isEmpty()) {
            List<Long> imageIds = pattern.getImages().stream()
                    .map(Image::getId)
                    .toList();
            List<Image> existingImages = imageRepository.findAllById(imageIds);
            pattern.setImages(existingImages);
        }

        Patterns postPattern = patternsRepository.save(pattern);
        return postPattern;
    }

    /**
     * updates the description of a pattern
     * @param username
     * @param patternName
     * @param description
     * @return
     */
    @Operation(
            summary = "Updates the description of a pattern",
            description = "Replaces the description text of a pattern belonging to a user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern updated successfully",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User or pattern not found")
    })
    @PutMapping("/patterns/{username}/{patternName}")
    Patterns updatePatternDescription(
            @Parameter(description = "Username of the pattern owner") @PathVariable String username,
            @Parameter(description = "Name of the pattern to update") @PathVariable String patternName,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New description text",
                    required = true
            )
            @RequestBody String description){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user, patternName)
                .orElseThrow(()-> new RuntimeException("Pattern not found"));

        pattern.setDescription(description.replaceAll("^\"|\"$", ""));
        Patterns updated = patternsRepository.save(pattern);
        return updated;
    }

    /**
     * gets all patterns whose title contains part of search
     * @param search the string to search for in patternNames
     * @return list of patterns from highest to lowest rated
     */
    @Operation(
            summary = "Search patterns by title",
            description = "Returns all patterns whose titles contain the search text, sorted from highest to lowest rating."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of matching patterns",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
    })
    @GetMapping("/patterns/title/{search}")
    List<Patterns> searchPatterns(@Parameter(description = "Search text for pattern names")
                                  @PathVariable String search){
        Optional<List<Patterns>> optResults = patternsRepository.findByPatternNameContaining(search);
        if(optResults.isEmpty()){
            return null;
        }
        List<Patterns> results = optResults.orElseThrow(() -> new RuntimeException("No results found"));
        results.sort(Comparator.comparing(Patterns::getRating).reversed());
        return results;
    }

    /**
     * gets all patterns posted by one author
     * @param username the user to search for
     * @return list of patterns from highest to lowest rated
     */
    @Operation(
            summary = "Get all patterns from a specific author",
            description = "Returns patterns uploaded by the given username, sorted from highest to lowest rating."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of patterns",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/patterns/author/{username}")
    List<Patterns> getUserPatterns(@Parameter(description = "Username whose patterns to retrieve")
                                   @PathVariable String username){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Patterns> patternsList = patternsRepository.findByUser(user)
                .orElseThrow(()-> new RuntimeException("no patterns for this user"));
        patternsList.sort(Comparator.comparing(Patterns::getRating).reversed());
        return patternsList;
    }

    /**
     * gets a pattern from username with patternName
     * @param username
     * @param patternName
     * @return pattern
     */
    @Operation(
            summary = "Get a specific pattern",
            description = "Fetches a pattern by username and pattern name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern found",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User or pattern not found")
    })
    @GetMapping("/patterns/{username}/{patternName}")
    Patterns getPattern(@Parameter(description = "Username of the pattern owner") @PathVariable String username,
                        @Parameter(description = "Pattern name") @PathVariable String patternName){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user, patternName)
                .orElseThrow(()-> new RuntimeException("Pattern not found"));

        return pattern;
    }

    /**
     * get all patterns posted users username follows and themselves
     * @param username
     * @return list of patterns from most recently posted to oldest
     */
    @Operation(
            summary = "Get patterns posted by user and followed users",
            description = "Returns patterns from the user and all users they follow, "
                    + "sorted from newest to oldest."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of patterns",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/patterns/{username}")
    List<Patterns> getFollowersPatterns(@Parameter(description = "Username whose feed to view")
                                        @PathVariable String username){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<List<Patterns>> patternsOpt = patternsRepository.findByUser(user);
        List<Patterns> patterns = new ArrayList<>();
        if(patternsOpt.isPresent()){
            patterns = patternsOpt.orElseThrow();
        }
        List<Follow> following = followRepository.findByFollower(user);
        for(int i=0; i<following.size(); i++){
            List<Patterns> userFollowing = patternsRepository.findByUser(following.get(i).getFollowing())
                    .orElseThrow(()-> new RuntimeException("Following not found"));
            patterns.addAll(userFollowing);
        }
        patterns.sort(Comparator.comparing(Patterns::getDate).reversed());
        return patterns;
    }

    @GetMapping("/patterns/id/{id}")
    Patterns getPatternById(@PathVariable Long id){
        Patterns pattern = patternsRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Following not found"));
        return pattern;
    }

    @PutMapping("/patterns/{username}/{patternName}/like")
    Patterns likePattern(@PathVariable String username, @PathVariable String patternName){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user, patternName)
                .orElseThrow(()-> new RuntimeException("Pattern not found"));
        pattern.setNumLikes(pattern.getNumLikes()+1);
        patternsRepository.save(pattern);
        return pattern;
    }

    @PutMapping("/patterns/{username}/{patternName}/unlike")
    Patterns unlikePattern(@PathVariable String username, @PathVariable String patternName){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user, patternName)
                .orElseThrow(()-> new RuntimeException("Pattern not found"));
        if(pattern.getNumLikes() > 0){
            pattern.setNumLikes(pattern.getNumLikes()-1);
            patternsRepository.save(pattern);
        }
        return pattern;
    }

    /**
     * private method that adds a rating to a pattern and updates the total rating
     * @param pattern
     * @param rating
     * @return
     */
    private float addRating(Patterns pattern, float rating){
        if(pattern.getRating() == 0){
            pattern.setRating(rating);
        }
        else{
            int numRatings = pattern.getNumRatings() + 1;
            float newRating = (pattern.getNumRatings() * pattern.getRating()) / numRatings + (rating / numRatings);
            pattern.setRating(newRating);
        }
        pattern.setNumRatings(pattern.getNumRatings()+1);
        patternsRepository.save(pattern);
        return pattern.getRating();
    }



    /**
     * Deletes pattern with patternName
     * @param patternName the name of the pattern to be deleted
     * @return Pattern not found, or patternName deleted
     */
    @Operation(
            summary = "Delete a pattern",
            description = "Deletes a pattern with a specific name belonging to the given user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pattern deleted"),
            @ApiResponse(responseCode = "404", description = "User or pattern not found")
    })
    @DeleteMapping("/patterns/{username}/{patternName}")
    String deletePattern(@Parameter(description = "Pattern name") @PathVariable String patternName,
                         @Parameter(description = "Username") @PathVariable String username){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<Patterns> optPattern = patternsRepository.findByUserAndPatternName(user, patternName);
        if(optPattern.isEmpty()){
            return "Pattern not found";
        }
        Patterns pattern = optPattern.orElseThrow(()-> new RuntimeException("Pattern not found"));
        patternsRepository.delete(pattern);
        return patternName + " deleted";
    }

    @GetMapping("/patterns/{username}/ratings")
    List<PatternsComments> usersRatings(@PathVariable String username){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<List<PatternsComments>> optList = patternsCommentsRepository.findByUserOrderByDateDesc(user);
        List<PatternsComments> ratings = new ArrayList<>();
        if(optList.isPresent()){
            ratings = optList.orElseThrow();
        }
        return ratings;
    }

    /**
     * adds a comment to a pattern
     * @param username user who posted pattern
     * @param patternName name of pattern
     * @param comment comment contents
     * @return the pattern with new comment
     */
    @Operation(
            summary = "Add a comment to a pattern",
            description = "Adds a new comment to the specified pattern. If the comment includes a rating, "
                    + "the pattern's rating is updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment added",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User or pattern not found")
    })
    @PostMapping("/patterns/{username}/{patternName}/comment/{commentUsername}")
    Patterns addComment(@Parameter(description = "Username") @PathVariable String username,
                        @Parameter(description = "Pattern name") @PathVariable String patternName,
                        @Parameter(description = "commenting username") @PathVariable String commentUsername,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                description = "Comment contents",
                                required = true,
                                content = @Content(schema = @Schema(implementation = PatternsComments.class))
                        )
                        @RequestBody PatternsComments comment){
        Users postingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(postingUser,patternName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        Users commentUser = userRepository.findByUsername(commentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        comment.setDate(LocalDateTime.now());
        comment.setPattern(pattern);
        comment.setUser(commentUser);
        patternsCommentsRepository.save(comment);
        if(comment.getRating() != null){
            addRating(pattern, comment.getRating());
        }
        return pattern;
    }

    /**
     * delete a comment from a post
     * @param username
     * @param patternName
     * @param id
     * @return pattern with updated comments
     */
    @Operation(
            summary = "Delete a comment from a pattern",
            description = "Removes a specific comment from the given pattern."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment deleted",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User, pattern, or comment not found")
    })
    @DeleteMapping("/patterns/{username}/{patternName}/{id}")
    Patterns deleteComment(@Parameter(description = "Username") @PathVariable String username,
                           @Parameter(description = "Pattern name") @PathVariable String patternName,
                           @Parameter(description = "Comment ID") @PathVariable Long id){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user,patternName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        PatternsComments comment = patternsCommentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        patternsCommentsRepository.delete(comment);
        return pattern;
    }

    /**
     * like a comment
     * @param username
     * @param patternName
     * @param id
     * @return updated pattern contents
     */
    @Operation(
            summary = "Like a comment",
            description = "Increases the like count on a comment by 1."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment liked",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User, pattern, or comment not found")
    })
    @PutMapping("/patterns/{username}/{patternName}/{id}/like")
    Patterns likeComment(@Parameter(description = "Username") @PathVariable String username,
                         @Parameter(description = "Pattern name") @PathVariable String patternName,
                         @Parameter(description = "Comment ID") @PathVariable Long id){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user,patternName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        PatternsComments comment = patternsCommentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setLikes(comment.getLikes()+1);
        patternsCommentsRepository.save(comment);
        return pattern;
    }

    /**
     * updates a comment
     * @param username
     * @param patternName
     * @param id
     * @param updatedComment
     * @return
     */
    @Operation(
            summary = "Update a comment",
            description = "Edits the text of a comment belonging to a particular pattern."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User, pattern, or comment not found")
    })
    @PutMapping("patterns/{username}/{patternName}/{id}")
    Patterns updateComment(@Parameter(description = "Username") @PathVariable String username,
                           @Parameter(description = "Pattern name") @PathVariable String patternName,
                           @Parameter(description = "Comment ID") @PathVariable Long id,
                           @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                   description = "Updated comment text",
                                   required = true
                           )
                           @RequestBody String updatedComment){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user,patternName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        PatternsComments comment = patternsCommentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setText(updatedComment.replaceAll("^\"|\"$", ""));

        patternsCommentsRepository.save(comment);
        return pattern;
    }

    /**
     * unlike a comment
     * @param username
     * @param patternName
     * @param id
     * @return updated pattern contents
     */
    @Operation(
            summary = "unlike a comment",
            description = "decreases the like count on a comment by 1."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment unliked",
                    content = @Content(schema = @Schema(implementation = Patterns.class))),
            @ApiResponse(responseCode = "404", description = "User, pattern, or comment not found")
    })
    @PutMapping("/patterns/{username}/{patternName}/{id}/unlike")
    Patterns unlikeComment(@Parameter(description = "Username") @PathVariable String username,
                         @Parameter(description = "Pattern name") @PathVariable String patternName,
                         @Parameter(description = "Comment ID") @PathVariable Long id){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user,patternName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        PatternsComments comment = patternsCommentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if(comment.getLikes() > 0){
            comment.setLikes(comment.getLikes()-1);
        }
        patternsCommentsRepository.save(comment);
        return pattern;
    }
}