package com.example.controller;

import com.example.model.Comment;
import com.example.model.Post;
import com.example.model.User;
import com.example.service.CommentService;
import com.example.service.FileUploadService;
import com.example.service.LikeService;
import com.example.service.PostService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/personal")
public class PersonalController {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping
    public String personalPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Post> userPosts = postService.getPostsByUser(user);
            long postCount = postService.getPostCountByUser(user);
            
            model.addAttribute("currentUser", user);
            model.addAttribute("user", user); // For navbar
            model.addAttribute("username", user.getUsername()); // For navbar
            model.addAttribute("posts", userPosts);
            model.addAttribute("postCount", postCount);
            model.addAttribute("newPost", new Post());
        }
        
        return "personal";
    }

    @PostMapping("/status")
    public String updateStatus(@RequestParam("status") String status, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (status.length() <= 200) {
                userService.updateUserStatus(user, status);
                redirectAttributes.addFlashAttribute("statusSuccess", "Status updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("statusError", "Status must be less than 200 characters!");
            }
        }
        
        return "redirect:/personal";
    }

    @PostMapping("/avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            try {
                if (!file.isEmpty()) {
                    // Delete old avatar if exists
                    if (user.getAvatarFilename() != null) {
                        fileUploadService.deleteAvatarFile(user.getAvatarFilename());
                    }
                    
                    // Save new avatar
                    String filename = fileUploadService.saveAvatarFile(file);
                    userService.updateUserAvatar(user, filename);
                    redirectAttributes.addFlashAttribute("avatarSuccess", "Avatar updated successfully!");
                } else {
                    redirectAttributes.addFlashAttribute("avatarError", "Please select an image file!");
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("avatarError", e.getMessage());
            }
        }
        
        return "redirect:/personal";
    }

    @PostMapping("/post")
    public String createPost(@Valid @ModelAttribute("newPost") Post post, 
                           BindingResult result, 
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("postError", "Post content is required and must be less than 1000 characters!");
            return "redirect:/personal";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            postService.createPost(post.getTitle(), post.getContent(), user);
            redirectAttributes.addFlashAttribute("postSuccess", "Post created successfully!");
        }
        
        return "redirect:/personal";
    }

    @PostMapping("/post/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Post> postOpt = postService.getPostById(id);
        
        if (userOpt.isPresent() && postOpt.isPresent()) {
            User user = userOpt.get();
            Post post = postOpt.get();
            
            if (postService.canUserDeletePost(user, post)) {
                postService.deletePost(id);
                redirectAttributes.addFlashAttribute("postSuccess", "Post deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("postError", "You can only delete your own posts!");
            }
        }
        
        return "redirect:/personal";
    }

    @GetMapping("/feed")
    public String viewFeed(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Post> posts = postService.getAllPosts(page, 10);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("user", currentUser); // For navbar
            model.addAttribute("username", currentUser.getUsername()); // For navbar
        }
        
        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());
        
        return "feed";
    }

    @GetMapping("/user/{username}")
    public String viewUserProfile(@PathVariable String username, Model model) {
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            List<Post> userPosts = postService.getPostsByUser(user);
            long postCount = postService.getPostCountByUser(user);
            
            // Check if current user is viewing their own profile
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isOwnProfile = auth.getName().equals(username);
            
            // Add current user for navbar
            Optional<User> currentUserOpt = userService.findByUsername(auth.getName());
            if (currentUserOpt.isPresent()) {
                User currentUser = currentUserOpt.get();
                model.addAttribute("user", currentUser); // For navbar
                model.addAttribute("username", currentUser.getUsername()); // For navbar
            }
            
            model.addAttribute("profileUser", user);
            model.addAttribute("posts", userPosts);
            model.addAttribute("postCount", postCount);
            model.addAttribute("isOwnProfile", isOwnProfile);
            
            return "user-profile";
        } else {
            return "redirect:/personal/feed";
        }
    }

    // New method for individual post page
    @GetMapping("/post/{id}")
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

    // Handle like/unlike
    @PostMapping("/post/{id}/like")
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
        
        return "redirect:/personal/post/" + id;
    }

    // Handle commenting
    @PostMapping("/post/{id}/comment")
    public String addComment(@PathVariable Long id, 
                           @Valid @ModelAttribute("newComment") Comment comment,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("commentError", "Comment content is required and must be less than 500 characters!");
            return "redirect:/personal/post/" + id;
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
        
        return "redirect:/personal/post/" + id;
    }

    // Handle comment replies
    @PostMapping("/post/{postId}/comment/{commentId}/reply")
    public String addReply(@PathVariable Long postId,
                          @PathVariable Long commentId,
                          @RequestParam("content") String content,
                          RedirectAttributes redirectAttributes) {
        if (content == null || content.trim().isEmpty() || content.length() > 500) {
            redirectAttributes.addFlashAttribute("commentError", "Reply content is required and must be less than 500 characters!");
            return "redirect:/personal/post/" + postId;
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
        
        return "redirect:/personal/post/" + postId;
    }

    // Handle comment deletion
    @PostMapping("/comment/{id}/delete")
    public String deleteComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Comment> commentOpt = commentService.getCommentById(id);
        
        if (userOpt.isPresent() && commentOpt.isPresent()) {
            User user = userOpt.get();
            Comment comment = commentOpt.get();
            Long postId = comment.getPost().getId();
            
            if (commentService.canUserDeleteComment(user, comment)) {
                commentService.deleteComment(id);
                redirectAttributes.addFlashAttribute("commentSuccess", "Comment deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("commentError", "You can only delete your own comments!");
            }
            
            return "redirect:/personal/post/" + postId;
        }
        
        return "redirect:/personal/feed";
    }

    @GetMapping("/markdown-guide")
    public String markdownGuide(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();
            model.addAttribute("user", currentUser); // For navbar
            model.addAttribute("username", currentUser.getUsername()); // For navbar
        }
        
        return "markdown-guide";
    }
} 