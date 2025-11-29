package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class TestQuestionRes {
    private Long id;
    private String questionType;
    private String mediaUrl;
    private String questionContent;
    private int difficult;
    private int maxScore;
    private int position;
    private String hiddenWord;
    private List<TestOptionRes> options;
}
