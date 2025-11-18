package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankItem {
    private Long profileId;
    private String nickName;
    private String avatarUrl;
    private long totalScore; // Tổng điểm thưởng (SUM(reward_count))

    // Rank sẽ được gán ở Service
    private int rank;

    // Constructor cho JPQL query (không cần rank)
    public RankItem(Long profileId, String nickName, String avatarUrl, long totalScore) {
        this.profileId = profileId;
        this.nickName = nickName;
        this.avatarUrl = avatarUrl;
        this.totalScore = totalScore;
    }
}
