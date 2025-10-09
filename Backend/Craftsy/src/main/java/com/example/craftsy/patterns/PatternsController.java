package com.example.craftsy.patterns;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class PatternsController {

    @Autowired
    PatternsRepository patternsRepository;
    @Autowired
    UserRepository userRepository;

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
        Patterns postPattern = patternsRepository.save(pattern);
        return pattern;
    }

    /**
     * Deletes pattern with patternName
     * @param patternName the name of the pattern to be deleted
     * @return Pattern not found, or patternName deleted
     */
    @DeleteMapping("/patterns/{patternName}")
    String deletePattern(@PathVariable String patternName){
        Optional<Patterns> optPattern = patternsRepository.findByPatternName(patternName);
        if(optPattern.isEmpty()){
            return "Pattern not found";
        }
        Patterns pattern = optPattern.orElseThrow(()-> new RuntimeException("Pattern not found"));
        patternsRepository.delete(pattern);
        return patternName + " deleted";
    }
}
