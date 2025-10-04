package com.example.craftsy.FollowingFollowers.Repository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.FollowingFollowers.Entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


// Gives ready made CRUD operations
public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow>findByFollower(Users follower); // Finds all follow relationships where a specific user is the follower.
    List<Follow>findByFollowing(Users following); // Finds all follow relationships where a specific user is being followed.
    Optional<Follow> findByFollowerAndFollowing(Users follower, Users following); // Finds a specific follow relationship between two users.

}