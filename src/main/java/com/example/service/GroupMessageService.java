package com.example.service;

import com.example.model.Group;
import com.example.model.GroupMessage;
import com.example.model.User;
import com.example.repository.GroupMessageRepository;
import com.example.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GroupMessageService {
    
    @Autowired
    private GroupMessageRepository groupMessageRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private GroupService groupService;
    
    public GroupMessage sendMessage(Group group, User sender, String content) {
        if (!groupService.isUserMember(group, sender)) {
            throw new IllegalStateException("User is not a member of this group");
        }
        
        GroupMessage message = new GroupMessage(content, group, sender);
        message = groupMessageRepository.save(message);
        
        // Update group's last activity
        group.setLastActivityAt(LocalDateTime.now());
        groupRepository.save(group);
        
        return message;
    }
    
    public Optional<GroupMessage> findById(Long id) {
        return groupMessageRepository.findById(id);
    }
    
    public List<GroupMessage> getGroupMessages(Group group) {
        return groupMessageRepository.findByGroupOrderByCreatedAtDesc(group);
    }
    
    public Page<GroupMessage> getGroupMessages(Group group, Pageable pageable) {
        return groupMessageRepository.findByGroupOrderByCreatedAtDesc(group, pageable);
    }
    
    public List<GroupMessage> getRecentMessages(Group group, LocalDateTime since) {
        return groupMessageRepository.findRecentMessages(group, since);
    }
    
    public List<GroupMessage> searchMessagesInGroup(Group group, String searchTerm) {
        return groupMessageRepository.searchInGroup(group, searchTerm);
    }
    
    public GroupMessage editMessage(GroupMessage message, String newContent, User editor) {
        if (!message.getSender().getId().equals(editor.getId())) {
            throw new IllegalStateException("Only the sender can edit their message");
        }
        
        // Only allow editing within 15 minutes
        if (message.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
            throw new IllegalStateException("Message can only be edited within 15 minutes");
        }
        
        message.setContent(newContent);
        message.markAsEdited();
        
        return groupMessageRepository.save(message);
    }
    
    public boolean deleteMessage(GroupMessage message, User deleter) {
        // Allow deletion by sender or group admin
        if (!message.getSender().getId().equals(deleter.getId()) && 
            !groupService.canUserManageGroup(message.getGroup(), deleter)) {
            return false;
        }
        
        groupMessageRepository.delete(message);
        return true;
    }
    
    public long getMessageCount(Group group) {
        return groupMessageRepository.countByGroup(group);
    }
    
    public long getRecentMessageCount(Group group, LocalDateTime since) {
        return groupMessageRepository.countRecentMessages(group, since);
    }
    
    public List<GroupMessage> getSystemMessages(Group group) {
        return groupMessageRepository.findSystemMessages(group);
    }
    
    public GroupMessage createSystemMessage(Group group, User actor, String content, GroupMessage.MessageType messageType) {
        GroupMessage systemMessage = new GroupMessage(content, group, actor, messageType);
        return groupMessageRepository.save(systemMessage);
    }
} 