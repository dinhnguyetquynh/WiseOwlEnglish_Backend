package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.UserAccountReq;
import com.iuh.WiseOwlEnglish_Backend.enums.RoleAccount;
import com.iuh.WiseOwlEnglish_Backend.model.UserAccount;
import com.iuh.WiseOwlEnglish_Backend.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserAccountService {

   private final UserAccountRepository userAccountRepository;
   private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
         this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount createUserAccount(UserAccountReq req) {
        if(userAccountRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        String encodePassword = passwordEncoder.encode(req.getPassword());
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(req.getEmail());
        userAccount.setPasswordHash(encodePassword);
        userAccount.setRoleAccount(RoleAccount.LEARNER);
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setUpdatedAt(LocalDateTime.now());
        return userAccountRepository.save(userAccount);

    }
}
