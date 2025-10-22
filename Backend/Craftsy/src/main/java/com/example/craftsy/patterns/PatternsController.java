package com.example.craftsy.patterns;

import com.example.craftsy.FollowingFollowers.Entity.Follow;
import com.example.craftsy.FollowingFollowers.Repository.FollowRepository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.feed.Feed;
import com.example.craftsy.images.Image;
import com.example.craftsy.images.ImageRepository;
import com.example.craftsy.patterns.patternsComments.PatternsComments;
import com.example.craftsy.patterns.patternsComments.PatternsCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
    @PostMapping("/patterns/{username}")
    Patterns postPattern(@RequestBody Patterns pattern, @PathVariable String username){
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

    @PutMapping("/patterns/{username}/{patternName}")
    Patterns updatePatternDescription(@PathVariable String username, @PathVariable String patternName, @RequestBody String description){
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
    @GetMapping("/patterns/title/{search}")
    List<Patterns> searchPatterns(@PathVariable String search){
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
    @GetMapping("/patterns/author/{username}")
    List<Patterns> getUserPatterns(@PathVariable String username){
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
    @GetMapping("/patterns/{username}/{patternName}")
    Patterns getPattern(@PathVariable String username, @PathVariable String patternName){
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
    @GetMapping("/patterns/{username}")
    List<Patterns> getFollowersPatterns(@PathVariable String username){
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
    @DeleteMapping("/patterns/{username}/{patternName}")
    String deletePattern(@PathVariable String patternName, @PathVariable String username){
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

    /**
     * adds a comment to a pattern
     * @param username user who posted pattern
     * @param patternName name of pattern
     * @param comment comment contents
     * @return the pattern with new comment
     */
    @PostMapping("/patterns/{username}/{patternName}/comment")
    Patterns addComment(@PathVariable String username, @PathVariable String patternName,
                                @RequestBody PatternsComments comment){
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternsRepository.findByUserAndPatternName(user,patternName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        comment.setDate(LocalDateTime.now());
        comment.setPattern(pattern);
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
    @DeleteMapping("/patterns/{username}/{patternName}/{id}")
    Patterns deleteComment(@PathVariable String username, @PathVariable String patternName,
                         @PathVariable Long id){
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
    @PutMapping("/patterns/{username}/{patternName}/{id}/like")
    Patterns likeComment(@PathVariable String username, @PathVariable String patternName,
                         @PathVariable Long id){
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

    @PutMapping("patterns/{username}/{patternName}/{id}")
    Patterns updateComment(@PathVariable String username, @PathVariable String patternName,
                           @PathVariable Long id, @RequestBody String updatedComment){
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
}