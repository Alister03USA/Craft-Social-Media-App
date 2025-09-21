package com.example.craftsy.SignUpDelete.Controller;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/signup")
    public ResponseEntity<Users> signup(@RequestBody Users user) {
        Users savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        Optional<Users> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            userRepository.delete(user.get()); // uses entity, transaction handled automatically
            return ResponseEntity.ok("User " + username + " deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}