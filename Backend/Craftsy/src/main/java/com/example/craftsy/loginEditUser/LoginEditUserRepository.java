package com.example.craftsy.loginEditUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginEditUserRepository extends JpaRepository<LoginEditUser, Long> {
    Optional<LoginEditUser> findByUsername(String username); //find user with given username
}
