package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LearnerProfileRes {
    private Long id;
    private String fullName;
    private String nickName;
    private LocalDate dateOfBirth;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String avatarUrl;
}

