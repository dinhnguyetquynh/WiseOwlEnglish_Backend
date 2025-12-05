package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonStatsRes {
    private Long lessonId;
    private String lessonName;
    private long totalLearners;       // Số người đã học xong
    private double completionRate;    // Tỷ lệ hoàn thành (%)
    private double averageTestScore;  // Điểm kiểm tra trung bình
}
