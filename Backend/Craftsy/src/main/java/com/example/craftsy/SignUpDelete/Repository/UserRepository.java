package com.example.craftsy.SignUpDelete.Repository;

import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


// Gives ready made CRUD operations
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByUsername(String username);

    List<Users> findByUsernameContainingIgnoreCase(String username);


}