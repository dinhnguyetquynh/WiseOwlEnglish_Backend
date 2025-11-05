package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class PictureWordRes {
    private Long id;
    private Long gameId;
    private int position;
    private String imgURL;
    private int rewardCore;
    private List<PictureWordOptRes> optsRes;
}
