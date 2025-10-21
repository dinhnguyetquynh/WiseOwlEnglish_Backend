package com.iuh.WiseOwlEnglish_Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LearnerProfileReq {
    @NotBlank
    private String fullName;
    private String nickName;
    @PastOrPresent
    private LocalDate dateOfBirth;

    @Size(max = 500)
    private String avatarUrl;
}
