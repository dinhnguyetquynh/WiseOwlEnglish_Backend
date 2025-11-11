package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameAnswerRes {
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private String correctAnswerText; // Đáp án đúng (text), để FE hiển thị nếu sai
    private int rewardEarned; // Điểm thưởng nhận được
}
