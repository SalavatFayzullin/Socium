package com.example.service;

import com.example.model.Chat;
import com.example.model.ChatMessage;
import com.example.model.User;
import com.example.repository.ChatRepository;
import com.example.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public Chat findOrCreateChat(User user1, User user2) {
        Optional<Chat> existingChat = chatRepository.findChatBetweenUsers(user1, user2);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        
        Chat newChat = new Chat(user1, user2);
        return chatRepository.save(newChat);
    }

    public ChatMessage sendMessage(Chat chat, User sender, String content) {
        ChatMessage message = new ChatMessage(content, chat, sender);
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // Update last message time in chat
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);
        
        return savedMessage;
    }

    public List<Chat> getUserChats(User user) {
        return chatRepository.findChatsByUser(user);
    }

    public List<ChatMessage> getChatMessages(Chat chat) {
        return chatMessageRepository.findByChatOrderByCreatedAtAsc(chat);
    }

    public Optional<Chat> getChatById(Long chatId) {
        return chatRepository.findById(chatId);
    }

    public long getUnreadMessageCount(Chat chat, User user) {
        return chatMessageRepository.countUnreadMessagesInChatForUser(chat, user);
    }

    public void markMessagesAsRead(Chat chat, User user) {
        chatMessageRepository.markMessagesAsReadInChatForUser(chat, user);
    }

    public boolean canUserAccessChat(Chat chat, User user) {
        return chat.hasUser(user);
    }

    public List<Chat> getChatsWithUnreadMessages(User user) {
        return chatRepository.findChatsWithUnreadMessages(user);
    }

    public ChatMessage getLatestMessage(Chat chat) {
        List<ChatMessage> messages = chatMessageRepository.findLatestMessageInChat(chat);
        return messages.isEmpty() ? null : messages.get(0);
    }
} 