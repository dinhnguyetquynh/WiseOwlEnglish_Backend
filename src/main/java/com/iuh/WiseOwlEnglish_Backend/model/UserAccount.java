package com.iuh.WiseOwlEnglish_Backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iuh.WiseOwlEnglish_Backend.enums.AccountStatus;
import com.iuh.WiseOwlEnglish_Backend.enums.RoleAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_accounts",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email")
})

public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_account", nullable = false, length = 30)
    private RoleAccount roleAccount = RoleAccount.LEARNER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @Column(name = "otp_hash", length = 255)
    private String otpHash;
    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;
    @Column(name = "otp_attempt")
    private int otpAttempt;
    @Column(name = "last_otp_sent_at")
    private LocalDateTime lastOtpSentAt;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Set<LearnerProfile> learnerProfiles = new HashSet<>();
}
