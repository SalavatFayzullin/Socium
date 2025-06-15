package com.example.repository;

import com.example.model.Chat;
import com.example.model.ChatMessage;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Find messages by chat ordered by creation time
    List<ChatMessage> findByChatOrderByCreatedAtAsc(Chat chat);
    
    // Count unread messages in a chat for a specific user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chat = :chat AND m.sender != :user AND m.isRead = false")
    long countUnreadMessagesInChatForUser(@Param("chat") Chat chat, @Param("user") User user);
    
    // Mark all messages in a chat as read for a specific user
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chat = :chat AND m.sender != :user AND m.isRead = false")
    void markMessagesAsReadInChatForUser(@Param("chat") Chat chat, @Param("user") User user);
    
    // Find latest message in a chat
    @Query("SELECT m FROM ChatMessage m WHERE m.chat = :chat ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessageInChat(@Param("chat") Chat chat);
} 