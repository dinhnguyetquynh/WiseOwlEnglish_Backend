package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

@Data
public class LessonOfGradeProgress {
    private long lessonId;
    private String unitName;
    private String lessonName;
    private int lessonProgress;
    private double lastTestScore;
}
