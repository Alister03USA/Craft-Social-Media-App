package com.example.craftsy.patterns;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.feed.Feed;
import com.example.craftsy.patterns.patternsComments.PatternsComments;
import com.example.craftsy.patterns.patternsComments.PatternsCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
        Patterns postPattern = patternsRepository.save(pattern);
        return pattern;
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
        return pattern;
    }

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

    @PutMapping("/patterns/{username}/{patternName}/{id}")
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
}