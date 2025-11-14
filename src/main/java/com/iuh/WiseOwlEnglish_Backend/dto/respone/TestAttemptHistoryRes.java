package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestAttemptHistoryRes {
    private Long testId;
    private String testTitle;
    private List<AttemptScore> attempts;
    @Data
    @AllArgsConstructor
    public static class AttemptScore {
        private Long attemptId;
        private double score;
        private LocalDateTime finishedAt;
    }
}