package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateLessonRes {
    private long id;
    private String unitNumber;
    private String unitName;
    private int orderIndex;
    private boolean active;
    private long gradeLevelId;
    private String urlMascot;
    private LocalDateTime updatedAt;
}
