package com.example.service;

import com.example.model.Comment;
import com.example.model.Post;
import com.example.model.User;
import com.example.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment createComment(String content, Post post, User author) {
        Comment comment = new Comment(content, post, author);
        return commentRepository.save(comment);
    }

    public Comment createReply(String content, Post post, User author, Comment parentComment) {
        Comment reply = new Comment(content, post, author, parentComment);
        return commentRepository.save(reply);
    }

    public List<Comment> getCommentsByPost(Post post) {
        return commentRepository.findByPostOrderByCreatedAtAsc(post);
    }

    public List<Comment> getTopLevelCommentsByPost(Post post) {
        return commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(post);
    }

    public List<Comment> getTopLevelCommentsWithReplies(Post post) {
        return commentRepository.findTopLevelCommentsWithReplies(post);
    }

    public List<Comment> getRepliesByComment(Comment parentComment) {
        return commentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment);
    }

    public List<Comment> getCommentsByUser(User author) {
        return commentRepository.findByAuthor(author);
    }

    public long getCommentCountByPost(Post post) {
        return commentRepository.countByPost(post);
    }

    public long getTopLevelCommentCountByPost(Post post) {
        return commentRepository.countByPostAndParentCommentIsNull(post);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public boolean canUserDeleteComment(User user, Comment comment) {
        return comment.getAuthor().getId().equals(user.getId());
    }

    public boolean hasUserCommentedOnPost(Post post, User user) {
        return commentRepository.existsByPostAndAuthor(post, user);
    }

    public void deleteCommentsByPost(Post post) {
        commentRepository.deleteByPost(post);
    }
} 