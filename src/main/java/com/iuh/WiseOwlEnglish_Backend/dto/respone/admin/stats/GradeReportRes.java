package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeReportRes {
    private long totalStudentsInGrade; // Tổng học sinh của khối
    private List<LessonStatsRes> lessons; // Danh sách thống kê bài học
}
