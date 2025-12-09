package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats;

import lombok.Data;

@Data
public class DataRes {
    private long totalLessons;
    private long totalVocabularies;
    private long totalSentences;
    private long totalGameQuestions;
    private long totalTestQuestions;
}
