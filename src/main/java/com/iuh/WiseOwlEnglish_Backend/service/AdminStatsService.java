package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats.*;
import com.iuh.WiseOwlEnglish_Backend.enums.LessonProgressStatus;
import com.iuh.WiseOwlEnglish_Backend.model.GameQuestion;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final SentenceRepository sentenceRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final TestQuestionRepository testQuestionRepository;
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

    public DataRes getTotalData(){
        long totalLesson = lessonRepo.countByDeletedAtIsNull();
        long totalVocab = vocabularyRepository.countByDeletedAtIsNull();
        long totalSen = sentenceRepository.countByDeletedAtIsNull();
        long totalGameQues = gameQuestionRepository.countByDeletedAtIsNull();
        long totalTestQues = testQuestionRepository.count();

        DataRes dataRes = new DataRes();
        dataRes.setTotalLessons(totalLesson);
        dataRes.setTotalVocabularies(totalVocab);
        dataRes.setTotalSentences(totalSen);
        dataRes.setTotalGameQuestions(totalGameQues);
        dataRes.setTotalTestQuestions(totalTestQues);
        return dataRes;
    }

    public List<DailyStatRes> getLearningActivityStats(LocalDate startDate, LocalDate endDate) {
        // 1. Lấy dữ liệu thô từ DB (chỉ chứa những ngày có người học)
        List<Object[]> rawData = lessonProgressRepo.countCompletedLessonsByDateRange(
                LessonProgressStatus.COMPLETED,
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        // 2. Chuyển đổi dữ liệu DB sang Map để dễ tra cứu <Ngày, Số lượng>
        Map<String, Long> statMap = new HashMap<>();
        for (Object[] row : rawData) {
            // Lưu ý: Tùy database mà kiểu dữ liệu ngày trả về có thể khác nhau (java.sql.Date hoặc String)
            String dateKey = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            statMap.put(dateKey, count);
        }

        // 3. Tạo danh sách đầy đủ các ngày từ start đến end (để ngày nào không có thì set = 0)
        List<DailyStatRes> result = new ArrayList<>();
        LocalDate current = startDate;

        DateTimeFormatter dbFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Format key so sánh
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd/MM"); // Format hiển thị FE

        while (!current.isAfter(endDate)) {
            String key = current.format(dbFormat);
            long count = statMap.getOrDefault(key, 0L);

            // Thêm thứ vào tên ngày cho dễ nhìn (VD: T2 10/12)
            String dayName = getVietnameseDayName(current);
            String label = dayName + " " + current.format(displayFormat);

            result.add(new DailyStatRes(label, count));
            current = current.plusDays(1);
        }

        return result;
    }

    private String getVietnameseDayName(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "T2";
            case TUESDAY: return "T3";
            case WEDNESDAY: return "T4";
            case THURSDAY: return "T5";
            case FRIDAY: return "T6";
            case SATURDAY: return "T7";
            case SUNDAY: return "CN";
            default: return "";
        }
    }


}
