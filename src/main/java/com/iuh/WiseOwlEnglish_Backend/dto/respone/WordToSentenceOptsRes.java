package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WordToSentenceOptsRes {
    private Long id;
    private Long questionId;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private int position;
    private String answerText;
}
