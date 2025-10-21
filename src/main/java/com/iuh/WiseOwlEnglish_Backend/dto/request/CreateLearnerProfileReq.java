package com.iuh.WiseOwlEnglish_Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
@Data
public class CreateLearnerProfileReq {
    private String fullName;
    private String nickName;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private Long initialGradeLevelId;
}
