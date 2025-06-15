package com.example.controller;

import com.example.model.Chat;
import com.example.model.ChatMessage;
import com.example.model.User;
import com.example.service.ChatService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, DeferredResult<ResponseEntity<Map<String, Object>>>> pendingRequests = new ConcurrentHashMap<>();

    @GetMapping("/{chatId}/poll")
    public DeferredResult<ResponseEntity<Map<String, Object>>> pollForMessages(
            @PathVariable Long chatId,
            @RequestParam(value = "lastMessageTime", required = false) String lastMessageTimeStr) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Chat> chatOpt = chatService.getChatById(chatId);
        
        if (!userOpt.isPresent() || !chatOpt.isPresent()) {
            DeferredResult<ResponseEntity<Map<String, Object>>> result = new DeferredResult<>();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Chat not found or access denied");
            result.setResult(ResponseEntity.badRequest().body(errorResponse));
            return result;
        }

        User currentUser = userOpt.get();
        Chat chat = chatOpt.get();

        if (!chatService.canUserAccessChat(chat, currentUser)) {
            DeferredResult<ResponseEntity<Map<String, Object>>> result = new DeferredResult<>();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Access denied");
            result.setResult(ResponseEntity.status(403).body(errorResponse));
            return result;
        }

        LocalDateTime lastMessageTime = null;
        if (lastMessageTimeStr != null && !lastMessageTimeStr.isEmpty()) {
            try {
                lastMessageTime = LocalDateTime.parse(lastMessageTimeStr);
            } catch (Exception e) {
                // If parsing fails, we'll check for all new messages
            }
        }

        // Check for new messages immediately
        List<ChatMessage> newMessages = getNewMessages(chat, lastMessageTime);
        if (!newMessages.isEmpty()) {
            // Mark messages as read for the current user
            chatService.markMessagesAsRead(chat, currentUser);
            
            DeferredResult<ResponseEntity<Map<String, Object>>> result = new DeferredResult<>();
            Map<String, Object> response = new HashMap<>();
            response.put("messages", convertMessagesToMap(newMessages, currentUser));
            response.put("timeout", false);
            result.setResult(ResponseEntity.ok(response));
            return result;
        }

        // Set up deferred result with 30 second timeout
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = new DeferredResult<>(30000L);
        
        // Set timeout callback
        deferredResult.onTimeout(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> timeoutResponse = new HashMap<>();
                timeoutResponse.put("timeout", true);
                timeoutResponse.put("messages", java.util.Collections.emptyList());
                deferredResult.setResult(ResponseEntity.ok(timeoutResponse));
            }
        });

        // For now, just return timeout response immediately to avoid scheduler issues
        Map<String, Object> timeoutResponse = new HashMap<>();
        timeoutResponse.put("timeout", true);
        timeoutResponse.put("messages", java.util.Collections.emptyList());
        deferredResult.setResult(ResponseEntity.ok(timeoutResponse));

        return deferredResult;
    }

    @GetMapping("/{chatId}/messages/latest")
    public ResponseEntity<Map<String, Object>> getLatestMessages(
            @PathVariable Long chatId,
            @RequestParam(value = "lastMessageTime", required = false) String lastMessageTimeStr) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        Optional<Chat> chatOpt = chatService.getChatById(chatId);
        
        if (!userOpt.isPresent() || !chatOpt.isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Chat not found or access denied");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        User currentUser = userOpt.get();
        Chat chat = chatOpt.get();

        if (!chatService.canUserAccessChat(chat, currentUser)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Access denied");
            return ResponseEntity.status(403).body(errorResponse);
        }

        LocalDateTime lastMessageTime = null;
        if (lastMessageTimeStr != null && !lastMessageTimeStr.isEmpty()) {
            try {
                lastMessageTime = LocalDateTime.parse(lastMessageTimeStr);
            } catch (Exception e) {
                // If parsing fails, return all messages
            }
        }

        List<ChatMessage> newMessages = getNewMessages(chat, lastMessageTime);
        
        // Mark messages as read
        if (!newMessages.isEmpty()) {
            chatService.markMessagesAsRead(chat, currentUser);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("messages", convertMessagesToMap(newMessages, currentUser));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/updates")
    public ResponseEntity<Map<String, Object>> getChatListUpdates() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOpt = userService.findByUsername(auth.getName());
        
        if (!userOpt.isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        User currentUser = userOpt.get();
        List<Chat> chats = chatService.getUserChats(currentUser);
        
        List<Map<String, Object>> chatUpdates = chats.stream().map(chat -> {
            Map<String, Object> chatMap = new HashMap<>();
            chatMap.put("id", chat.getId());
            
            ChatMessage latestMessage = chatService.getLatestMessage(chat);
            if (latestMessage != null) {
                chatMap.put("lastMessage", latestMessage.getContent());
                chatMap.put("lastMessageTime", latestMessage.getCreatedAt().toString());
            }
            
            long unreadCount = chatService.getUnreadMessageCount(chat, currentUser);
            chatMap.put("unreadCount", unreadCount);
            
            return chatMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("chats", chatUpdates);
        return ResponseEntity.ok(response);
    }

    private List<ChatMessage> getNewMessages(Chat chat, LocalDateTime lastMessageTime) {
        List<ChatMessage> allMessages = chatService.getChatMessages(chat);
        
        if (lastMessageTime == null) {
            return allMessages;
        }
        
        return allMessages.stream()
                .filter(message -> message.getCreatedAt().isAfter(lastMessageTime))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertMessagesToMap(List<ChatMessage> messages, User currentUser) {
        return messages.stream().map(message -> {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("content", message.getContent());
            messageMap.put("createdAt", message.getCreatedAt().toString());
            messageMap.put("isOwn", message.getSender().getId().equals(currentUser.getId()));
            
            Map<String, Object> senderMap = new HashMap<>();
            senderMap.put("id", message.getSender().getId());
            senderMap.put("username", message.getSender().getUsername());
            senderMap.put("avatarUrl", message.getSender().getAvatarUrl());
            messageMap.put("sender", senderMap);
            
            return messageMap;
        }).collect(Collectors.toList());
    }
} 