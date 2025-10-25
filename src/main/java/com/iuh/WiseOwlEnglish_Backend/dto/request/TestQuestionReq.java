package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class TestQuestionReq {
    private String questionType;
    private String stemType;
    private Long stemRefId;
    private String stemText;
    private Integer difficulty;
    private Integer maxScore;
    private Integer orderInTest;
    List<TestOptionReq> options;
}
