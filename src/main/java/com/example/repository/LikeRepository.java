package com.example.repository;

import com.example.model.Like;
import com.example.model.Post;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    // Check if user has liked a post
    boolean existsByPostAndUser(Post post, User user);
    
    // Find like by post and user
    Optional<Like> findByPostAndUser(Post post, User user);
    
    // Count likes by post
    long countByPost(Post post);
    
    // Delete likes by post (for cleanup)
    void deleteByPost(Post post);
    
    // Delete specific like by post and user
    void deleteByPostAndUser(Post post, User user);
} 