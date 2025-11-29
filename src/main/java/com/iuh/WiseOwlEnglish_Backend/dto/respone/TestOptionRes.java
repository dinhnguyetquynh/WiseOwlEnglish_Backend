package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TestOptionRes {
    private Long id;
    private String optionText;
    private String imgUrl;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private int position;
    private String side;
    private String pairKey;
}
