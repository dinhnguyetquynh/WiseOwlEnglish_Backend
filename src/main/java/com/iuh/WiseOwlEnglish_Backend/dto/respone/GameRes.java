package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

@Data
public class GameRes {
    private Long id;
    private String title;
    private String type;
    private int difficulty;
    private Long lessonId;
    private Long correctAudio;
    private Long wrongAudio;
}
