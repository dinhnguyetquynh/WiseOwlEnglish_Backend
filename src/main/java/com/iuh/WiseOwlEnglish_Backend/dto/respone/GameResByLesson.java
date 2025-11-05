package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

@Data
public class GameResByLesson {
    private Long id;
    private String title;
    private String gameType;
    private int difficulty;

}
