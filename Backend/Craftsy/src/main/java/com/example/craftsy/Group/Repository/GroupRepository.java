package com.example.craftsy.Group.Repository;

import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupName(String groupName);
    List<Group> findByGroupNameContainingIgnoreCaseOrGroupAdmin_UsernameContainingIgnoreCase(String groupName, String username);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members WHERE g.id = :id")
    Optional<Group> findByIdWithMembers(@Param("id") Long id);
}
