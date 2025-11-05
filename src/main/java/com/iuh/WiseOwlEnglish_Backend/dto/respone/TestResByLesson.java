package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

@Data
public class TestResByLesson {
    private Long id;
    private Long lessonId;
    private String title;
    private String type;
    private String description;
    private Integer durationMin;
    private Boolean active;
}
