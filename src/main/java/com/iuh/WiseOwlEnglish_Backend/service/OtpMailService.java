package com.iuh.WiseOwlEnglish_Backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpMailService {
    private final JavaMailSender mailSender;
    public OtpMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    public void sendOtpEmail(String to, String otp) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("[WiseOwl English] Mã xác thực OTP");
        msg.setText("""
                Xin chào,

                Mã OTP của bạn là: %s
                OTP có hiệu lực trong 10 phút. Tuyệt đối không chia sẻ OTP cho bất kỳ ai.

                Trân trọng,
                WiseOwl English Team
                """.formatted(otp));
        mailSender.send(msg);
    }
}
