package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PictureSentenceOptRes {
    private Long id;
    private Long questionId;
    private String sentenceAnswer;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private int position;
}
