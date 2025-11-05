package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class WordToSentenceRes {
    private Long id;
    private Long gameId;
    private int position;
    private int rewardCore;
    private String questionText;
    private List<WordToSentenceOptsRes> opts;
}
