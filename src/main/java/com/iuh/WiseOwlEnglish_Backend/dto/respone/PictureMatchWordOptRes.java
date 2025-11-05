package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PictureMatchWordOptRes {
    private Long id;
    private Long questionId;
    private String imgUrl;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private String side;
    private String pairKey;
    private int position;
    private String answerText;
}
