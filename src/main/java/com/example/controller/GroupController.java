package com.example.controller;

import com.example.model.*;
import com.example.service.GroupService;
import com.example.service.GroupMessageService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/groups")
public class GroupController {
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private GroupMessageService groupMessageService;
    
    @Autowired
    private UserService userService;
    
    private User getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName()).orElse(null);
    }
    
    // List user's groups
    @GetMapping
    public String listGroups(Model model, Principal principal,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> userGroups = groupService.getUserGroups(currentUser, pageable);
        
        model.addAttribute("groups", userGroups);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userGroups.getTotalPages());
        
        return "groups/list";
    }
    
    // Discover public groups
    @GetMapping("/discover")
    public String discoverGroups(Model model, Principal principal,
                               @RequestParam(defaultValue = "") String search,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> publicGroups;
        
        if (search.isEmpty()) {
            publicGroups = groupService.searchPublicGroups("", pageable);
        } else {
            publicGroups = groupService.searchPublicGroups(search, pageable);
        }
        
        model.addAttribute("groups", publicGroups);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", publicGroups.getTotalPages());
        
        return "groups/discover";
    }
    
    // Show group creation form
    @GetMapping("/create")
    public String showCreateGroupForm(Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        return "groups/create";
    }
    
    // Create a new group
    @PostMapping("/create")
    public String createGroup(@RequestParam String name,
                            @RequestParam String description,
                            @RequestParam(defaultValue = "false") boolean isPublic,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            Group group = groupService.createGroup(name, description, currentUser);
            group.setPublic(isPublic);
            
            redirectAttributes.addFlashAttribute("success", "Group created successfully!");
            return "redirect:/groups/" + group.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating group: " + e.getMessage());
            return "redirect:/groups/create";
        }
    }
    
    // View a specific group
    @GetMapping("/{id}")
    public String viewGroup(@PathVariable Long id, Model model, Principal principal,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        // Check if user is a member
        if (!groupService.isUserMember(group, currentUser)) {
            return "redirect:/groups/" + id + "/join";
        }
        
        // Get messages
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupMessage> messages = groupMessageService.getGroupMessages(group, pageable);
        
        // Get members
        List<GroupMember> members = groupService.getGroupMembers(group);
        
        model.addAttribute("group", group);
        model.addAttribute("messages", messages);
        model.addAttribute("members", members);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", groupService.canUserManageGroup(group, currentUser));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        
        return "groups/view";
    }
    
    // Join group page
    @GetMapping("/{id}/join")
    public String showJoinGroupPage(@PathVariable Long id, Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        // If already a member, redirect to group
        if (groupService.isUserMember(group, currentUser)) {
            return "redirect:/groups/" + id;
        }
        
        model.addAttribute("group", group);
        model.addAttribute("currentUser", currentUser);
        
        return "groups/join";
    }
    
    // Join a group
    @PostMapping("/{id}/join")
    public String joinGroup(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group not found");
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        if (groupService.joinGroup(group, currentUser)) {
            redirectAttributes.addFlashAttribute("success", "Successfully joined " + group.getName() + "!");
            return "redirect:/groups/" + id;
        } else {
            redirectAttributes.addFlashAttribute("error", "Unable to join group");
            return "redirect:/groups/" + id + "/join";
        }
    }
    
    // Join group via invite code
    @GetMapping("/join/{inviteCode}")
    public String joinGroupByInviteCode(@PathVariable String inviteCode, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findByInviteCode(inviteCode);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid invite code");
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        if (groupService.joinGroup(group, currentUser)) {
            redirectAttributes.addFlashAttribute("success", "Successfully joined " + group.getName() + "!");
            return "redirect:/groups/" + group.getId();
        } else {
            redirectAttributes.addFlashAttribute("error", "Unable to join group");
            return "redirect:/groups";
        }
    }
    
    // Leave a group
    @PostMapping("/{id}/leave")
    public String leaveGroup(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group not found");
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        if (groupService.leaveGroup(group, currentUser)) {
            redirectAttributes.addFlashAttribute("success", "Left " + group.getName() + " successfully");
            return "redirect:/groups";
        } else {
            redirectAttributes.addFlashAttribute("error", "Unable to leave group");
            return "redirect:/groups/" + id;
        }
    }
    
    // Send message to group
    @PostMapping("/{id}/message")
    public String sendMessage(@PathVariable Long id, @RequestParam String content, 
                            Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group not found");
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        try {
            groupMessageService.sendMessage(group, currentUser, content);
            redirectAttributes.addFlashAttribute("success", "Message sent!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error sending message: " + e.getMessage());
        }
        
        return "redirect:/groups/" + id;
    }
    
    // Group settings page
    @GetMapping("/{id}/settings")
    public String showGroupSettings(@PathVariable Long id, Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        if (!groupService.canUserManageGroup(group, currentUser)) {
            return "redirect:/groups/" + id;
        }
        
        List<GroupMember> members = groupService.getGroupMembers(group);
        
        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("inviteLink", group.getInviteLink());
        
        return "groups/settings";
    }
    
    // Update group settings
    @PostMapping("/{id}/settings")
    public String updateGroupSettings(@PathVariable Long id,
                                    @RequestParam String name,
                                    @RequestParam String description,
                                    @RequestParam(defaultValue = "false") boolean isPublic,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group not found");
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        Group updatedGroup = groupService.updateGroup(group, name, description, isPublic, currentUser);
        if (updatedGroup != null) {
            redirectAttributes.addFlashAttribute("success", "Group settings updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Unable to update group settings");
        }
        
        return "redirect:/groups/" + id + "/settings";
    }
    
    // Regenerate invite code
    @PostMapping("/{id}/regenerate-invite")
    public String regenerateInviteCode(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group not found");
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        String newCode = groupService.regenerateInviteCode(group, currentUser);
        if (newCode != null) {
            redirectAttributes.addFlashAttribute("success", "New invite code generated!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Unable to regenerate invite code");
        }
        
        return "redirect:/groups/" + id + "/settings";
    }
    
    // Delete group
    @PostMapping("/{id}/delete")
    public String deleteGroup(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        Optional<Group> groupOpt = groupService.findById(id);
        if (groupOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Group not found");
            return "redirect:/groups";
        }
        
        Group group = groupOpt.get();
        
        if (groupService.deleteGroup(group, currentUser)) {
            redirectAttributes.addFlashAttribute("success", "Group deleted successfully");
            return "redirect:/groups";
        } else {
            redirectAttributes.addFlashAttribute("error", "Unable to delete group");
            return "redirect:/groups/" + id + "/settings";
        }
    }
} 