package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AnswerReq {
    private Long questionId;
    private Long optionId;               // single-choice / true-false
    private List<Long> optionIds;          // multiple-select (nếu có)
    private List<Long> sequence;           // ordering
    private List<PairDTO> pairs;           // matching
    private String textInput;              // fill in blank
    private String numericInput;
}
