package com.example.service;

import com.example.model.Like;
import com.example.model.Post;
import com.example.model.User;
import com.example.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    public boolean toggleLike(Post post, User user) {
        if (isPostLikedByUser(post, user)) {
            // Unlike the post
            likeRepository.deleteByPostAndUser(post, user);
            return false; // Post is now unliked
        } else {
            // Like the post
            Like like = new Like(post, user);
            likeRepository.save(like);
            return true; // Post is now liked
        }
    }

    public boolean isPostLikedByUser(Post post, User user) {
        return likeRepository.existsByPostAndUser(post, user);
    }

    public long getLikeCountByPost(Post post) {
        return likeRepository.countByPost(post);
    }

    public Optional<Like> getLikeByPostAndUser(Post post, User user) {
        return likeRepository.findByPostAndUser(post, user);
    }

    public void deleteLikesByPost(Post post) {
        likeRepository.deleteByPost(post);
    }

    public Like createLike(Post post, User user) {
        if (!isPostLikedByUser(post, user)) {
            Like like = new Like(post, user);
            return likeRepository.save(like);
        }
        return null; // Already liked
    }

    public boolean removeLike(Post post, User user) {
        if (isPostLikedByUser(post, user)) {
            likeRepository.deleteByPostAndUser(post, user);
            return true; // Successfully removed
        }
        return false; // Like didn't exist
    }
} 