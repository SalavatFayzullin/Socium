package com.example.repository;

import com.example.model.Group;
import com.example.model.GroupMessage;
import com.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    
    List<GroupMessage> findByGroupOrderByCreatedAtDesc(Group group);
    
    Page<GroupMessage> findByGroupOrderByCreatedAtDesc(Group group, Pageable pageable);
    
    List<GroupMessage> findByGroupOrderByCreatedAtAsc(Group group);
    
    Page<GroupMessage> findByGroupOrderByCreatedAtAsc(Group group, Pageable pageable);
    
    List<GroupMessage> findBySender(User sender);
    
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group = :group AND gm.createdAt > :since ORDER BY gm.createdAt ASC")
    List<GroupMessage> findRecentMessages(@Param("group") Group group, @Param("since") LocalDateTime since);
    
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group = :group AND gm.content LIKE %:searchTerm% ORDER BY gm.createdAt DESC")
    List<GroupMessage> searchInGroup(@Param("group") Group group, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(gm) FROM GroupMessage gm WHERE gm.group = :group")
    long countByGroup(@Param("group") Group group);
    
    @Query("SELECT COUNT(gm) FROM GroupMessage gm WHERE gm.group = :group AND gm.createdAt > :since")
    long countRecentMessages(@Param("group") Group group, @Param("since") LocalDateTime since);
    
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group = :group AND gm.messageType != 'TEXT' ORDER BY gm.createdAt DESC")
    List<GroupMessage> findSystemMessages(@Param("group") Group group);
} 