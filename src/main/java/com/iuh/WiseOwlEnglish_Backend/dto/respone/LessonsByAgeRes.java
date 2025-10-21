package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;
@Data
public class LessonsByAgeRes {
    private Long profileId;
    private int age;
    private Long gradeLevelId;
    private String gradeName;
    private int gradeOrderIndex;
    private List<LessonBriefRes> lessons;
}
