package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class LessonByClassRes {
    private Long profileId;
    private Long gradeLevelId;
    private String gradeName;
    private int gradeOrderIndex;
    private List<LessonBriefRes> lessons;
}
