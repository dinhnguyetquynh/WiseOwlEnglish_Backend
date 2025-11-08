package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class LessonWithGamesDTO {
    private Long lessonId;
    private String unitName;
    private String lessonName;
    private List<GameInfoDTO> games; // Danh sách các game thuộc lesson này
}
