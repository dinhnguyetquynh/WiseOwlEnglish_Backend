package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;
import org.apache.catalina.LifecycleState;

import java.util.List;

@Data
public class PictureGuessingGameRes {
    private Long id;
    private Long gameId;
    private int position;
    private String imageUrl;
    private int reward;
    private List<PictureGuessingGameOptionRes> options;

}
