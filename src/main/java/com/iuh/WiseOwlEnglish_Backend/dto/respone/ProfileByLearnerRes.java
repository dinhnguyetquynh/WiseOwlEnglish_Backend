package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProfileByLearnerRes {
    private long learnerId;
    private String fullName;
    private String nickName;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private int numberDayStudied;
}
