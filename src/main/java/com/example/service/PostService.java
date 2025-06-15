package com.example.service;

import com.example.model.Post;
import com.example.model.User;
import com.example.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    public Post createPost(String content, User author) {
        Post post = new Post(content, author);
        return postRepository.save(post);
    }

    public List<Post> getPostsByUser(User author) {
        return postRepository.findByAuthorOrderByCreatedAtDesc(author);
    }

    public Page<Post> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<Post> getPostsByUser(User author, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }

    public List<Post> getRecentPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findRecentPosts(pageable);
    }

    public long getPostCountByUser(User author) {
        return postRepository.countByAuthor(author);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public boolean canUserDeletePost(User user, Post post) {
        return post.getAuthor().getId().equals(user.getId());
    }

    public long getTotalPosts() {
        return postRepository.count();
    }
} 