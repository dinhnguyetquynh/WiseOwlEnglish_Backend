package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonByGradeRes {
    private Long id;
    private String unitName;
    private String lessonName;
    private int orderIndex;
    private boolean active;
    private LocalDateTime updatedAt;
    private String mascot;

}
