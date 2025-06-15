package com.example.service;

import com.example.model.*;
import com.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GroupService {
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    
    @Autowired
    private GroupMessageRepository groupMessageRepository;
    
    @Autowired
    private GroupInvitationRepository groupInvitationRepository;
    
    public Group createGroup(String name, String description, User creator) {
        Group group = new Group(name, description, creator);
        group = groupRepository.save(group);
        
        // Add creator as admin
        GroupMember creatorMember = new GroupMember(group, creator, GroupMember.MemberRole.ADMIN);
        groupMemberRepository.save(creatorMember);
        
        // Create system message for group creation
        GroupMessage creationMessage = new GroupMessage(
            "Group created by " + creator.getUsername(),
            group,
            creator,
            GroupMessage.MessageType.GROUP_CREATED
        );
        groupMessageRepository.save(creationMessage);
        
        return group;
    }
    
    public Optional<Group> findById(Long id) {
        return groupRepository.findById(id);
    }
    
    public Optional<Group> findByInviteCode(String inviteCode) {
        return groupRepository.findByInviteCode(inviteCode);
    }
    
    public List<Group> getUserGroups(User user) {
        return groupRepository.findByUserMember(user);
    }
    
    public Page<Group> getUserGroups(User user, Pageable pageable) {
        return groupRepository.findByUserMember(user, pageable);
    }
    
    public List<Group> getPublicGroups() {
        return groupRepository.findByIsPublicTrueOrderByLastActivityAtDesc();
    }
    
    public Page<Group> searchPublicGroups(String searchTerm, Pageable pageable) {
        return groupRepository.searchPublicGroups(searchTerm, pageable);
    }
    
    public boolean isUserMember(Group group, User user) {
        return groupMemberRepository.existsByGroupAndUser(group, user);
    }
    
    public boolean isUserAdmin(Group group, User user) {
        Optional<GroupMember> member = groupMemberRepository.findByGroupAndUser(group, user);
        return member.isPresent() && member.get().isAdmin();
    }
    
    public boolean canUserManageGroup(Group group, User user) {
        if (group.isCreator(user)) {
            return true;
        }
        
        Optional<GroupMember> member = groupMemberRepository.findByGroupAndUser(group, user);
        return member.isPresent() && member.get().canManageGroup();
    }
    
    @Transactional
    public boolean joinGroup(Group group, User user) {
        if (isUserMember(group, user)) {
            return false; // Already a member
        }
        
        if (group.getMemberCount() >= group.getMaxMembers()) {
            return false; // Group full
        }
        
        GroupMember member = new GroupMember(group, user, GroupMember.MemberRole.MEMBER);
        groupMemberRepository.save(member);
        
        // Create system message
        GroupMessage joinMessage = new GroupMessage(
            user.getUsername() + " joined the group",
            group,
            user,
            GroupMessage.MessageType.USER_JOINED
        );
        groupMessageRepository.save(joinMessage);
        
        // Update group activity
        group.setLastActivityAt(LocalDateTime.now());
        groupRepository.save(group);
        
        return true;
    }
    
    @Transactional
    public boolean leaveGroup(Group group, User user) {
        if (!isUserMember(group, user)) {
            return false;
        }
        
        // Creator cannot leave unless they transfer ownership or delete group
        if (group.isCreator(user)) {
            long adminCount = groupMemberRepository.findByGroupAndRole(group, GroupMember.MemberRole.ADMIN).size();
            if (adminCount <= 1) {
                return false; // Need to have another admin before leaving
            }
        }
        
        groupMemberRepository.deleteByGroupAndUser(group, user);
        
        // Create system message
        GroupMessage leaveMessage = new GroupMessage(
            user.getUsername() + " left the group",
            group,
            user,
            GroupMessage.MessageType.USER_LEFT
        );
        groupMessageRepository.save(leaveMessage);
        
        // Update group activity
        group.setLastActivityAt(LocalDateTime.now());
        groupRepository.save(group);
        
        return true;
    }
    
    @Transactional
    public boolean removeUserFromGroup(Group group, User userToRemove, User remover) {
        if (!canUserManageGroup(group, remover)) {
            return false;
        }
        
        if (!isUserMember(group, userToRemove)) {
            return false;
        }
        
        // Cannot remove the creator
        if (group.isCreator(userToRemove)) {
            return false;
        }
        
        groupMemberRepository.deleteByGroupAndUser(group, userToRemove);
        
        // Create system message
        GroupMessage removeMessage = new GroupMessage(
            userToRemove.getUsername() + " was removed from the group by " + remover.getUsername(),
            group,
            remover,
            GroupMessage.MessageType.USER_REMOVED
        );
        groupMessageRepository.save(removeMessage);
        
        // Update group activity
        group.setLastActivityAt(LocalDateTime.now());
        groupRepository.save(group);
        
        return true;
    }
    
    public Group updateGroup(Group group, String name, String description, boolean isPublic, User updater) {
        if (!canUserManageGroup(group, updater)) {
            return null;
        }
        
        group.setName(name);
        group.setDescription(description);
        group.setPublic(isPublic);
        group.setLastActivityAt(LocalDateTime.now());
        
        return groupRepository.save(group);
    }
    
    public boolean deleteGroup(Group group, User deleter) {
        if (!group.isCreator(deleter)) {
            return false;
        }
        
        groupRepository.delete(group);
        return true;
    }
    
    public List<GroupMember> getGroupMembers(Group group) {
        return groupMemberRepository.findByGroupOrderByJoinedAt(group);
    }
    
    public boolean promoteToAdmin(Group group, User userToPromote, User promoter) {
        if (!canUserManageGroup(group, promoter)) {
            return false;
        }
        
        Optional<GroupMember> member = groupMemberRepository.findByGroupAndUser(group, userToPromote);
        if (member.isEmpty()) {
            return false;
        }
        
        member.get().setRole(GroupMember.MemberRole.ADMIN);
        groupMemberRepository.save(member.get());
        
        return true;
    }
    
    public String regenerateInviteCode(Group group, User user) {
        if (!canUserManageGroup(group, user)) {
            return null;
        }
        
        String newCode = UUID.randomUUID().toString().substring(0, 8);
        group.setInviteCode(newCode);
        groupRepository.save(group);
        
        return newCode;
    }
} 