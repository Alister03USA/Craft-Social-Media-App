package com.example.craftsy.feed;

import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedRespository extends JpaRepository<Feed, Long> {
    Optional<List<Feed>> findByUser(Users user);
    Optional<Feed> findByUserAndProjectName(Users user, String projectName);
}
