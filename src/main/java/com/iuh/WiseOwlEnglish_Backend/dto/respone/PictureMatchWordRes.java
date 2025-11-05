package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class PictureMatchWordRes {
    private Long id;
    private Long gameId;
    private int position;
    private int rewardCore;
    private List<PictureMatchWordOptRes> optRes;
}
