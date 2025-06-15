package com.example.controller;

import com.example.model.User;
import com.example.service.DatabaseBackupService;
import com.example.service.UserService;
import com.example.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class DatabaseController {

    @Autowired
    private DatabaseBackupService backupService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @GetMapping("/database")
    public String databaseManagement(Model model, Principal principal) {
        try {
            // Add current user for navbar
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> userOpt = userService.findByUsername(auth.getName());
            if (userOpt.isPresent()) {
                User currentUser = userOpt.get();
                model.addAttribute("user", currentUser); // For navbar
                model.addAttribute("username", currentUser.getUsername()); // For navbar
            }
            
            long totalUsers = userService.getTotalUsers();
            long totalPosts = postService.getTotalPosts();
            long databaseSize = backupService.getDatabaseSize();
            String formattedSize = backupService.formatFileSize(databaseSize);

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalPosts", totalPosts);
            model.addAttribute("databaseSize", formattedSize);
            model.addAttribute("rawDatabaseSize", databaseSize);

            return "admin/database";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading database information: " + e.getMessage());
            return "admin/database";
        }
    }

    @PostMapping("/database/backup")
    public String createBackup(RedirectAttributes redirectAttributes) {
        try {
            String backupFileName = backupService.createBackup();
            redirectAttributes.addFlashAttribute("success", 
                "Database backup created successfully: " + backupFileName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error creating backup: " + e.getMessage());
        }
        return "redirect:/admin/database";
    }
} 