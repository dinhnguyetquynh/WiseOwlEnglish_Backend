package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PronounceGradeResponse {
    private String grade; // ACCURATE | ALMOST | INACCURATE
    private int score;    // 0..100
    private String feedback;
}
