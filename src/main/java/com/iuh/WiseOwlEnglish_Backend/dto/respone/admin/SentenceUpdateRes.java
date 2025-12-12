package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SentenceUpdateRes {
    private Long id;
    private int orderIndex;
    private String sentence_en;
    private String sentence_vi;
    private LocalDateTime updatedAt;
    private boolean isForLearning;
    private String imgUrl;
    private String audioNormal;
    private String audioSlow;
}
