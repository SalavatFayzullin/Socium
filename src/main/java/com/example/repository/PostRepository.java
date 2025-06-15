package com.example.repository;

import com.example.model.Post;
import com.example.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Find posts by author
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    
    // Find all posts ordered by creation date (newest first)
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Find posts by author with pagination
    Page<Post> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
    
    // Count posts by author
    long countByAuthor(User author);
    
    // Find recent posts (for feed)
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findRecentPosts(Pageable pageable);
} 