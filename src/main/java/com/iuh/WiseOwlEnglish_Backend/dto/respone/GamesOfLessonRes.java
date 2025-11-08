package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class GamesOfLessonRes {
    private Long lessonId;
    private String unitName;
    private String lessonName;
    private List<GameDetailRes> games;
}
