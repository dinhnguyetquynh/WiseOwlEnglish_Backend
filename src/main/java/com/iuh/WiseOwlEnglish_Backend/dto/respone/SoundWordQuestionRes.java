package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class SoundWordQuestionRes {
    private Long id;
    private int position;
    private String urlSound;
    private int rewardPoint;
    private List<SoundWordOptionRes> options;

}
