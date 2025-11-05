package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class PictureSentenceQuesRes {
    private Long id;
    private Long gameId;
    private int position;
    private String sentenceQues;//cau hoi
    private String imageUrl;
    private int rewardPoint;
    private List<PictureSentenceOptRes> options;
}
