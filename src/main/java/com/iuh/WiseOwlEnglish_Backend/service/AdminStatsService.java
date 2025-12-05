package com.iuh.WiseOwlEnglish_Backend.service;

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
    public LearnerStatsRes getLearnerStats() {
        LearnerStatsRes res = new LearnerStatsRes();
        res.setTotalLearners(learnerRepo.count());
        res.setTotalUserAccounts(userRepo.count());
        res.setGradeDistribution(gradeProgressRepo.countLearnersByGrade());

        List<Object[]> growthData = learnerRepo.countNewLearnersByMonth();
        res.setMonthlyGrowth(growthData.stream()
                .map(row -> new LearnerStatsRes.MonthlyGrowth((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList()));

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
