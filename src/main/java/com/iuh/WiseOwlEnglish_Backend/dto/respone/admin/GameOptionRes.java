package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import lombok.Data;

@Data
public class GameOptionRes {
    private long id;
    private String contentType;
    private Long contentRefId;
    private String answerText;
    private boolean correct;
    private String side;
    private String pairKey;
}
