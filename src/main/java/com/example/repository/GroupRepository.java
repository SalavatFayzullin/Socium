package com.example.repository;

import com.example.model.Group;
import com.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    Optional<Group> findByInviteCode(String inviteCode);
    
    List<Group> findByCreatedBy(User createdBy);
    
    List<Group> findByIsPublicTrueOrderByLastActivityAtDesc();
    
    Page<Group> findByIsPublicTrueOrderByLastActivityAtDesc(Pageable pageable);
    
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user = :user ORDER BY g.lastActivityAt DESC")
    List<Group> findByUserMember(@Param("user") User user);
    
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user = :user ORDER BY g.lastActivityAt DESC")
    Page<Group> findByUserMember(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT g FROM Group g WHERE g.name LIKE %:searchTerm% OR g.description LIKE %:searchTerm%")
    List<Group> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT g FROM Group g WHERE g.isPublic = true AND (g.name LIKE %:searchTerm% OR g.description LIKE %:searchTerm%)")
    Page<Group> searchPublicGroups(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM GroupMember m WHERE m.group = :group")
    long countMembersByGroup(@Param("group") Group group);
} 