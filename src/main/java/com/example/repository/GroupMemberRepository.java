package com.example.repository;

import com.example.model.Group;
import com.example.model.GroupMember;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    
    List<GroupMember> findByGroup(Group group);
    
    List<GroupMember> findByUser(User user);
    
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group ORDER BY gm.joinedAt ASC")
    List<GroupMember> findByGroupOrderByJoinedAt(@Param("group") Group group);
    
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.role = :role")
    List<GroupMember> findByGroupAndRole(@Param("group") Group group, @Param("role") GroupMember.MemberRole role);
    
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group = :group")
    long countByGroup(@Param("group") Group group);
    
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.user = :user")
    long countByUser(@Param("user") User user);
    
    boolean existsByGroupAndUser(Group group, User user);
    
    void deleteByGroupAndUser(Group group, User user);
} 