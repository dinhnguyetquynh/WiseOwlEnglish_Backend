package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SoundWordOptionRes {
    private long id;
    private long gameQuestionId;
    private String optionText;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private int position;
}
