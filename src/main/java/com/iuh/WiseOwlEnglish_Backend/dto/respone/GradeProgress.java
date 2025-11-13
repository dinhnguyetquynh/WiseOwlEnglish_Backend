package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class GradeProgress {
    private int orderIndex;
    private int lessonsLearned;
    private int rewardScore;
    private int starsArchived;
    private List<LessonOfGradeProgress> listLessons;
}
