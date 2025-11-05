package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class SubmitTestRes {
    private Long attemptId;
    private Long testId;
    private double score;
    private int correctCount;
    private int wrongCount;
    private int questionCount;
    private int durationSec;
    private List<QuestionResultRes> questionResults;
}
