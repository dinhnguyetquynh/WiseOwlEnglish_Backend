package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestResByLesson;
import lombok.Data;

import java.util.List;
@Data
public class LessonWithTestsRes {
    private Long lessonId;
    private String unitName;
    private String lessonName;
    private int orderIndex; // Để sắp xếp hiển thị
    private List<TestResByLesson> tests;
}
