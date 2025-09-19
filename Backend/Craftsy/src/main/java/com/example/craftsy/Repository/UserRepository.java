package com.example.craftsy.Repository;

import com.example.craftsy.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;


// Gives ready made CRUD operations
public interface UserRepository extends JpaRepository<Users, Long> {

}