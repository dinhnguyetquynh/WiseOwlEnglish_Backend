package com.iuh.WiseOwlEnglish_Backend.dto.request;

import lombok.Data;

@Data
public class GameOptionReq {
    private String contentType;
    private Long contentRefId;
    private String answerText;
    private boolean correct;
    private String side;
    private String pairKey;
}
