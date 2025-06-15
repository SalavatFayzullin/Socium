package com.example.repository;

import com.example.model.Chat;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    // Find chat between two users
    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)")
    Optional<Chat> findChatBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    // Find all chats for a user, ordered by last message time
    @Query("SELECT c FROM Chat c WHERE c.user1 = :user OR c.user2 = :user ORDER BY c.lastMessageAt DESC")
    List<Chat> findChatsByUser(@Param("user") User user);
    
    // Find chats with unread messages for a user
    @Query("SELECT DISTINCT c FROM Chat c JOIN c.messages m WHERE (c.user1 = :user OR c.user2 = :user) AND m.isRead = false AND m.sender != :user")
    List<Chat> findChatsWithUnreadMessages(@Param("user") User user);
} 