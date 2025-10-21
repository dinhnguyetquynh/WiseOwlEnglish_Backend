package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

@Data
public class VerifyOtpReq {
    private String email;
    private String otp;
}
