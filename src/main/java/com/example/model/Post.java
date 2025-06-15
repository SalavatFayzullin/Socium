package com.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Post title is required")
    @Size(max = 200, message = "Post title must be less than 200 characters")
    @Column(name = "title")
    private String title;

    @NotBlank(message = "Post content is required")
    @Size(max = 5000, message = "Post content must be less than 5000 characters")
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    public Post() {}

    public Post(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public Post(String content, User author) {
        this.title = "Untitled Post";
        this.content = content;
        this.author = author;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    // Helper methods
    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }

    public int getCommentCount() {
        return comments != null ? comments.size() : 0;
    }

    public String getHtmlContent() {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        // Simple markdown-to-HTML conversion
        String html = content
            // Headers
            .replaceAll("(?m)^### (.*?)$", "<h3>$1</h3>")
            .replaceAll("(?m)^## (.*?)$", "<h2>$1</h2>")
            .replaceAll("(?m)^# (.*?)$", "<h1>$1</h1>")
            // Bold and italic
            .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
            .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
            // Code
            .replaceAll("`(.*?)`", "<code>$1</code>")
            // Links
            .replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\"$2\">$1</a>")
            // Line breaks
            .replaceAll("\\n", "<br>");
            
        return html;
    }

    public long getViewCount() {
        // For now, return a placeholder value
        // This would typically be stored in the database
        return 0;
    }

    public boolean isLikedByUser(Object user) {
        // This method would check if the current user has liked this post
        // For now, return false as a placeholder
        return false;
    }
} 