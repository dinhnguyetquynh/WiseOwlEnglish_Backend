package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class QuestionResultRes {
   private Long questionId;
    private String questionType;
    private boolean correct;
    private Double earnedScore;
    private Integer maxScore;
    private Long selectedOptionId;
    private List<Long> selectedOptionIds;
    private List<Long> correctOptionIds; // đây là
}
