package com.example.controller;

import com.example.model.Comment;
import com.example.model.Post;
import com.example.model.User;
import com.example.service.CommentService;
import com.example.service.LikeService;
import com.example.service.PostService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Optional<Post> postOpt = postService.getPostById(id);
        
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            // Get hierarchical comments (top-level with replies)
            List<Comment> comments = commentService.getTopLevelCommentsWithReplies(post);
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> userOpt = userService.findByUsername(auth.getName());
            
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();
                boolean isLikedByUser = likeService.isPostLikedByUser(post, currentUser);
                boolean canDeletePost = postService.canUserDeletePost(currentUser, post);
                
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("user", currentUser); // For navbar
                model.addAttribute("username", currentUser.getUsername()); // For navbar
                model.addAttribute("isLikedByUser", isLikedByUser);
                model.addAttribute("canDeletePost", canDeletePost);
            }
            
            long likeCount = likeService.getLikeCountByPost(post);
            long commentCount = commentService.getCommentCountByPost(post);
            
            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            model.addAttribute("likeCount", likeCount);
            model.addAttribute("commentCount", commentCount);
            model.addAttribute("newComment", new Comment());
            
            return "post-detail";
        } else {
            return "redirect:/personal/feed";
        }
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id, 
                           @Valid @ModelAttribute("newComment") Comment comment,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("commentError", "Comment content is required and must be less than 500 characters!");
            return "redirect:/post/" + id;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Post> postOpt = postService.getPostById(id);
        
        if (userOpt.isPresent() && postOpt.isPresent()) {
            User user = userOpt.get();
            Post post = postOpt.get();
            
            commentService.createComment(comment.getContent(), post, user);
            redirectAttributes.addFlashAttribute("commentSuccess", "Comment added successfully!");
        }
        
        return "redirect:/post/" + id;
    }

    @PostMapping("/{postId}/comment/{commentId}/reply")
    public String addReply(@PathVariable Long postId,
                          @PathVariable Long commentId,
                          @RequestParam("content") String content,
                          RedirectAttributes redirectAttributes) {
        if (content == null || content.trim().isEmpty() || content.length() > 500) {
            redirectAttributes.addFlashAttribute("commentError", "Reply content is required and must be less than 500 characters!");
            return "redirect:/post/" + postId;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Post> postOpt = postService.getPostById(postId);
        Optional<Comment> parentCommentOpt = commentService.getCommentById(commentId);
        
        if (userOpt.isPresent() && postOpt.isPresent() && parentCommentOpt.isPresent()) {
            User user = userOpt.get();
            Post post = postOpt.get();
            Comment parentComment = parentCommentOpt.get();
            
            commentService.createReply(content, post, user, parentComment);
            redirectAttributes.addFlashAttribute("commentSuccess", "Reply added successfully!");
        }
        
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{postId}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long postId, 
                               @PathVariable Long commentId, 
                               RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Comment> commentOpt = commentService.getCommentById(commentId);
        
        if (userOpt.isPresent() && commentOpt.isPresent()) {
            User user = userOpt.get();
            Comment comment = commentOpt.get();
            
            if (commentService.canUserDeleteComment(user, comment)) {
                commentService.deleteComment(commentId);
                redirectAttributes.addFlashAttribute("commentSuccess", "Comment deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("commentError", "You can only delete your own comments!");
            }
        }
        
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{id}/like")
    public String toggleLike(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Post> postOpt = postService.getPostById(id);
        
        if (userOpt.isPresent() && postOpt.isPresent()) {
            User user = userOpt.get();
            Post post = postOpt.get();
            
            boolean isLiked = likeService.toggleLike(post, user);
            
            if (isLiked) {
                redirectAttributes.addFlashAttribute("likeSuccess", "Post liked!");
            } else {
                redirectAttributes.addFlashAttribute("likeSuccess", "Post unliked!");
            }
        }
        
        return "redirect:/post/" + id;
    }
} 