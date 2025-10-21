package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.UserAccountReq;
import com.iuh.WiseOwlEnglish_Backend.enums.AccountStatus;
import com.iuh.WiseOwlEnglish_Backend.enums.RoleAccount;
import com.iuh.WiseOwlEnglish_Backend.exception.EmailAlreadyExistsException;
import com.iuh.WiseOwlEnglish_Backend.model.UserAccount;
import com.iuh.WiseOwlEnglish_Backend.repository.UserAccountRepository;
import com.iuh.WiseOwlEnglish_Backend.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpMailService otpMailService;

    @Transactional
    public UserAccount createUserAccount(UserAccountReq req) {
        if (userAccountRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException(req.getEmail());
        }
        var now = LocalDateTime.now();
        var user = new UserAccount();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        System.out.println("Encoded password: " + user.getPasswordHash());
        user.setRoleAccount(RoleAccount.LEARNER); // (có thể bạn muốn LEARNER?)
        user.setStatus(AccountStatus.UNVERIFIED);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // tạo OTP 6 số
        String otp = OtpUtils.generateNumeric(6);
        user.setOtpHash(passwordEncoder.encode(otp));
        user.setOtpExpiresAt(now.plusMinutes(10));
        user.setOtpAttempt(0);
        user.setLastOtpSentAt(now);

        var saved = userAccountRepository.save(user);

        // gửi email OTP
        otpMailService.sendOtpEmail(saved.getEmail(), otp);

        return saved;
    }
    @Transactional
    public void resendOtp(String email) {
        var user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
        if (user.getStatus() != AccountStatus.UNVERIFIED) {
            throw new IllegalStateException("Account already verified");
        }
        var now = LocalDateTime.now();
        if (user.getLastOtpSentAt() != null && now.isBefore(user.getLastOtpSentAt().plusMinutes(1))) {
            throw new IllegalStateException("Please wait before requesting another OTP");
        }
        String otp = OtpUtils.generateNumeric(6);
        user.setOtpHash(passwordEncoder.encode(otp));
        user.setOtpExpiresAt(now.plusMinutes(10));
        user.setOtpAttempt(0);
        user.setLastOtpSentAt(now);
        otpMailService.sendOtpEmail(user.getEmail(), otp);
        userAccountRepository.save(user);
    }
    @Transactional
    public void verifyOtp(String email, String otp) {
        var user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        if (user.getStatus() != AccountStatus.UNVERIFIED) {
            throw new IllegalStateException("Account already verified");
        }
        if (user.getOtpExpiresAt() == null || LocalDateTime.now().isAfter(user.getOtpExpiresAt())) {
            throw new IllegalStateException("OTP expired");
        }
        if (user.getOtpAttempt() >= 5) {
            throw new IllegalStateException("Too many attempts. Please resend OTP.");
        }

        boolean match = passwordEncoder.matches(otp, user.getOtpHash());
        if (!match) {
            user.setOtpAttempt(user.getOtpAttempt() + 1);
            userAccountRepository.save(user);
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Thành công: kích hoạt tài khoản
        user.setStatus(AccountStatus.VERIFIED);
        user.setOtpHash(null);
        user.setOtpExpiresAt(null);
        user.setOtpAttempt(0);
        user.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(user);
    }

}
