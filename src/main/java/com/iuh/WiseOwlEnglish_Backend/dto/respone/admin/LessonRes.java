package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonRes {
    private long id;
    private String unitNumber;
    private String unitName;
    private int orderIndex;
    private boolean active;
    private String urlMascot;
    private LocalDateTime updatedAt;
}
