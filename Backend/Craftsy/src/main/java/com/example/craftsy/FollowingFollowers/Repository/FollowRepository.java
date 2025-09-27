package com.example.craftsy.FollowingFollowers.Repository;
import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.FollowingFollowers.Entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


// Gives ready made CRUD operations
public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow>findByFollower(Users followers);
    List<Follow>findByFollowing(Users following);
    Optional<Follow> findByFollowerAndFollowing(Users follower, Users following);

}