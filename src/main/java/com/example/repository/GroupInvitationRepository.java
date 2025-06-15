package com.example.repository;

import com.example.model.Group;
import com.example.model.GroupInvitation;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    
    Optional<GroupInvitation> findByInvitationToken(String invitationToken);
    
    List<GroupInvitation> findByInvitedUser(User invitedUser);
    
    List<GroupInvitation> findByInvitedEmail(String invitedEmail);
    
    List<GroupInvitation> findByGroup(Group group);
    
    List<GroupInvitation> findByInvitedBy(User invitedBy);
    
    @Query("SELECT gi FROM GroupInvitation gi WHERE gi.invitedUser = :user AND gi.status = 'PENDING' AND gi.expiresAt > :now")
    List<GroupInvitation> findPendingInvitationsForUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Query("SELECT gi FROM GroupInvitation gi WHERE gi.invitedEmail = :email AND gi.status = 'PENDING' AND gi.expiresAt > :now")
    List<GroupInvitation> findPendingInvitationsByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    @Query("SELECT gi FROM GroupInvitation gi WHERE gi.group = :group AND gi.status = 'PENDING' AND gi.expiresAt > :now")
    List<GroupInvitation> findPendingInvitationsForGroup(@Param("group") Group group, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(gi) FROM GroupInvitation gi WHERE gi.group = :group AND gi.status = 'PENDING'")
    long countPendingInvitationsByGroup(@Param("group") Group group);
    
    @Query("SELECT gi FROM GroupInvitation gi WHERE gi.expiresAt < :now AND gi.status = 'PENDING'")
    List<GroupInvitation> findExpiredInvitations(@Param("now") LocalDateTime now);
    
    boolean existsByGroupAndInvitedUserAndStatus(Group group, User invitedUser, GroupInvitation.InvitationStatus status);
    
    boolean existsByGroupAndInvitedEmailAndStatus(Group group, String invitedEmail, GroupInvitation.InvitationStatus status);
} 