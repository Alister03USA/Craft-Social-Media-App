package com.example.craftsy.Group.Repository;


import com.example.craftsy.Group.Entity.GroupJoinRequest;
import com.example.craftsy.Group.Entity.Group;
import com.example.craftsy.SignUpDelete.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long>{

    // Check if request is already sent
    Optional<GroupJoinRequest> findByGroupAndUser(Group group, Users user);

    // Delete all join requests for a group
    void deleteAllByGroup(Group group);
}
