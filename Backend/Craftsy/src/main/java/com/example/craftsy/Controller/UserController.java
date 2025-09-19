package com.example.craftsy.Controller;

import com.example.craftsy.Entity.Users;
import com.example.craftsy.Repository.UserRepository;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Optional<Users> user = userRepository.findById(id);
        if (user.isPresent()) {
            String username = user.get().getUsername();
            userRepository.deleteById(id);
            return ResponseEntity.ok( username + " deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}