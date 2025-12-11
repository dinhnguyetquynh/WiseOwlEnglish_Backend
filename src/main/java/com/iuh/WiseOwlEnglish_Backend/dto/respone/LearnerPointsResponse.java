package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearnerPointsResponse {
    private int pointBalance;      // Số dư điểm hiện tại trong ví (từ LearnerProfile)
    private Long totalRewardCount; // Tổng số điểm thưởng tích lũy từ tất cả các lần chơi (từ GameAttempt)
}
