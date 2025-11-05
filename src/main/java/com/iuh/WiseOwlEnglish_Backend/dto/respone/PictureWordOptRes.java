package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PictureWordOptRes {
    private Long id;
    private Long questionId;
    private String answerText;
    private int position;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
}
