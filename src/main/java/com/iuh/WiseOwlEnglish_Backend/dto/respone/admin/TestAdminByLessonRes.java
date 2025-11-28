package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import lombok.Data;

@Data
public class TestAdminByLessonRes {
    private Long id;
    private Long lessonId;
    private String title;
    private String type;
    private String description;
    private Integer durationMin;
    private Boolean active;


    private int totalQuestion; // Tổng số câu hỏi
    private boolean hasAttempt; // True nếu đã có ít nhất 1 người làm bài
}
