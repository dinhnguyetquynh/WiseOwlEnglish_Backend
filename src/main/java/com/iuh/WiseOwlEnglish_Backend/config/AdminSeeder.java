package com.iuh.WiseOwlEnglish_Backend.config;

import com.iuh.WiseOwlEnglish_Backend.enums.AccountStatus;
import com.iuh.WiseOwlEnglish_Backend.enums.RoleAccount;
import com.iuh.WiseOwlEnglish_Backend.model.UserAccount;
import com.iuh.WiseOwlEnglish_Backend.repository.UserAccountRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // Lấy giá trị từ application-secret.properties
    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    public AdminSeeder(UserAccountRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                log.info("⚠️  Admin credentials not set. Skipping admin seeding.");
                return;
            }

            var existing = userRepo.findByEmail(adminEmail);
            if (existing.isPresent()) {
                var user = existing.get();
                if (user.getRoleAccount() != RoleAccount.ADMIN) {
                    user.setRoleAccount(RoleAccount.ADMIN);
                    userRepo.save(user);
                    log.info("✅ Promoted existing user '{}' to ADMIN.", adminEmail);
                } else {
                    log.info("ℹ️  Admin '{}' already exists, skipping seeding.", adminEmail);
                }
                return;
            }

            UserAccount admin = new UserAccount();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRoleAccount(RoleAccount.ADMIN);
            admin.setStatus(AccountStatus.VERIFIED);
            admin.setCreatedAt(LocalDateTime.now());

            userRepo.save(admin);
            log.info("🎉 Created initial ADMIN account: {}", adminEmail);

        } catch (Exception e) {
            log.error("❌ Failed to seed admin account", e);
        }
    }
}
