package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class LessonProgressDetailRes {
    private Long lessonId;
    private String unitName;
    private String lessonName;
    private List<IncorrectItemRes> incorrectVocabularies;
    private List<IncorrectItemRes> incorrectSentences;
    private List<TestAttemptHistoryRes> testHistories;
}
