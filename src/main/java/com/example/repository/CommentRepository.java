package com.example.repository;

import com.example.model.Comment;
import com.example.model.Post;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Find comments by post ordered by creation date
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    
    // Find top-level comments (not replies) by post
    List<Comment> findByPostAndParentCommentIsNullOrderByCreatedAtAsc(Post post);
    
    // Find replies to a specific comment
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);
    
    // Find comments by author
    List<Comment> findByAuthor(User author);
    
    // Count comments by post (including replies)
    long countByPost(Post post);
    
    // Count top-level comments by post (excluding replies)
    long countByPostAndParentCommentIsNull(Post post);
    
    // Delete comments by post (for cleanup)
    void deleteByPost(Post post);
    
    // Check if user has commented on a post
    boolean existsByPostAndAuthor(Post post, User author);
    
    // Get comment hierarchy for a post (with replies loaded)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.post = :post AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsWithReplies(@Param("post") Post post);
} 