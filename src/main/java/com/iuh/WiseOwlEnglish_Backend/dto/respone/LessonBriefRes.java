package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

@Data
public class LessonBriefRes {
    private Long id;
    private String unitName;
    private String lessonName;
    private int orderIndex;
    private int percentComplete;
    private String status;
}
