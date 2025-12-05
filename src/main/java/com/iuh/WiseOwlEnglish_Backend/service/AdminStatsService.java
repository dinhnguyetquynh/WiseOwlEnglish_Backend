package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.GradeDistribution;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.GradeReportRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.LearnerStatsRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.LessonStatsRes;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService {
    private final LearnerProfileRepository learnerRepo;
    private final UserAccountRepository userRepo;
    private final LearnerGradeProgressRepository gradeProgressRepo;
    private final LessonRepository lessonRepo;
    private final LessonProgressRepository lessonProgressRepo;
    private final TestAttemptRepository testAttemptRepo;
    // 1. Thống kê người học
    @Transactional(readOnly = true)
    // Sửa signature hàm để nhận year
    public LearnerStatsRes getLearnerStats(int year) {
        LearnerStatsRes res = new LearnerStatsRes();
        res.setTotalLearners(learnerRepo.count());
        res.setTotalUserAccounts(userRepo.count());

        // 1. Phân bổ lớp (Giữ nguyên logic cũ)
        List<Object[]> rawGrades = gradeProgressRepo.countLearnersByGradeRaw();
        List<GradeDistribution> gradeDist = rawGrades.stream()
                .map(row -> new GradeDistribution(
                        "Lớp " + row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
        res.setGradeDistribution(gradeDist);

        // 2. Thống kê theo tháng (Logic MỚI)
        List<Object[]> rawGrowth = learnerRepo.countNewLearnersByYear(year);

        // Tạo map để tra cứu nhanh: tháng -> số lượng
        Map<Integer, Long> monthlyData = rawGrowth.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(), // month (1-12)
                        row -> ((Number) row[1]).longValue() // count
                ));

        // Tạo danh sách đủ 12 tháng
        List<LearnerStatsRes.MonthlyGrowth> growthStats = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            long count = monthlyData.getOrDefault(m, 0L);
            // Label dạng "Tháng 1", "Tháng 2"...
            growthStats.add(new LearnerStatsRes.MonthlyGrowth("T" + m, count));
        }

        res.setMonthlyGrowth(growthStats);

        return res;
    }
    // 2. Thống kê chi tiết bài học theo Lớp (Grade)
    @Transactional(readOnly = true)
    public GradeReportRes getLessonStatsByGrade(Long gradeId) { // 👈 Đổi kiểu trả về
        // 1. Lấy tổng số học sinh của lớp
        long totalStudentsInGrade = gradeProgressRepo.countTotalLearnersInGrade(gradeId);
        long calcBase = (totalStudentsInGrade == 0) ? 1 : totalStudentsInGrade; // Tránh chia cho 0

        List<Lesson> lessons = lessonRepo.findByGradeLevel_IdAndDeletedAtIsNullOrderByOrderIndexAsc(gradeId);
        List<LessonStatsRes> statsList = new ArrayList<>();

        for (Lesson l : lessons) {
            long completedCount = lessonProgressRepo.countCompletedByLessonId(l.getId());
            Double avgScore = testAttemptRepo.getAverageScoreByLessonId(l.getId());

            LessonStatsRes dto = new LessonStatsRes();
            dto.setLessonId(l.getId());
            dto.setLessonName(l.getUnitName() + ": " + l.getLessonName());
            dto.setTotalLearners(completedCount);

            // Tính % hoàn thành
            double rate = ((double) completedCount / calcBase) * 100;
            dto.setCompletionRate(Math.round(rate * 100.0) / 100.0);

            dto.setAverageTestScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);

            statsList.add(dto);
        }

        // Trả về Object bao gồm cả tổng số học sinh
        return new GradeReportRes(totalStudentsInGrade, statsList);
    }
}
