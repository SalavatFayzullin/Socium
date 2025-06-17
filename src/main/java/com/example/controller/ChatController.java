package com.example.controller;

import com.example.model.Chat;
import com.example.model.ChatMessage;
import com.example.model.User;
import com.example.service.ChatService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String chatList(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();
            List<Chat> chats = chatService.getUserChats(currentUser);
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("user", currentUser); // For navbar
            model.addAttribute("username", currentUser.getUsername()); // For navbar
            model.addAttribute("chats", chats);
            model.addAttribute("chatService", chatService); // For accessing service methods in template
        }
        
        return "chat-list";
    }

    @GetMapping("/{chatId}")
    public String viewChat(@PathVariable Long chatId, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Chat> chatOpt = chatService.getChatById(chatId);
        
        if (userOpt.isPresent() && chatOpt.isPresent()) {
            User currentUser = userOpt.get();
            Chat chat = chatOpt.get();
            
            // Check if user has access to this chat
            if (!chatService.canUserAccessChat(chat, currentUser)) {
                redirectAttributes.addFlashAttribute("error", "You don't have access to this chat!");
                return "redirect:/chat";
            }
            
            // Mark messages as read
            chatService.markMessagesAsRead(chat, currentUser);
            
            List<ChatMessage> messages = chatService.getChatMessages(chat);
            User otherUser = chat.getOtherUser(currentUser);
            
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("user", currentUser); // For navbar
            model.addAttribute("username", currentUser.getUsername()); // For navbar
            model.addAttribute("chat", chat);
            model.addAttribute("messages", messages);
            model.addAttribute("otherUser", otherUser);
            model.addAttribute("newMessage", new ChatMessage());
            
            return "chat-view";
        }
        
        return "redirect:/chat";
    }

    @PostMapping("/{chatId}/send")
    public String sendMessage(@PathVariable Long chatId, 
                             @RequestParam("content") String content,
                             RedirectAttributes redirectAttributes) {
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Message cannot be empty!");
            return "redirect:/chat/" + chatId;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Chat> chatOpt = chatService.getChatById(chatId);
        
        if (userOpt.isPresent() && chatOpt.isPresent()) {
            User currentUser = userOpt.get();
            Chat chat = chatOpt.get();
            
            if (chatService.canUserAccessChat(chat, currentUser)) {
                chatService.sendMessage(chat, currentUser, content);
                redirectAttributes.addFlashAttribute("success", "Message sent!");
            } else {
                redirectAttributes.addFlashAttribute("error", "You don't have access to this chat!");
            }
        }
        
        return "redirect:/chat/" + chatId;
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam(value = "query", required = false) String query, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("user", currentUser); // For navbar
            model.addAttribute("username", currentUser.getUsername()); // For navbar
            
            if (query != null && !query.trim().isEmpty()) {
                List<User> searchResults = userService.searchUsersByUsername(query);
                // Remove current user from search results
                searchResults.removeIf(user -> user.getId().equals(currentUser.getId()));
                model.addAttribute("users", searchResults);
                model.addAttribute("query", query);
            }
        }
        
        return "chat-search";
    }

    @PostMapping("/start/{userId}")
    public String startChat(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> currentUserOpt = userService.findByUsername(auth.getName());
        Optional<User> otherUserOpt = userService.findById(userId);
        
        if (currentUserOpt.isPresent() && otherUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            User otherUser = otherUserOpt.get();
            
            if (currentUser.getId().equals(otherUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "You cannot start a chat with yourself!");
                return "redirect:/chat/search";
            }
            
            Chat chat = chatService.findOrCreateChat(currentUser, otherUser);
            return "redirect:/chat/" + chat.getId();
        }
        
        redirectAttributes.addFlashAttribute("error", "User not found!");
        return "redirect:/chat/search";
    }
} 