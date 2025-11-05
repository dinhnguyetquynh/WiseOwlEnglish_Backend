package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class SentenceHiddenRes {
    private Long id;
    private Long gameId;
    private int position;
    private String imgURL;
    private String questionText;
    private String hiddenWord;
    private int rewardCore;
    private List<SentenceHiddenOptRes> optRes;
}
