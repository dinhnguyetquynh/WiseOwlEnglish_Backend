package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.GradeProgress;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonOfGradeProgress;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.GradeLevel;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.model.LessonProgress;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GradeProgressService {
    private final LessonProgressRepository lessonProgressRepo;
    private final LessonRepository lessonRepository;
    private final GameAttemptRepository gameAttemptRepo; // Đổi tên để khớp với repo
    private final GradeLevelRepository gradeLevelRepo; // Thêm repo này
    private final TestAttemptRepository testAttemptRepo; // Thêm repo này
    private final LearnerProfileRepository learnerProfileRepo; // Thêm repo này

    @Transactional(readOnly = true)
    public GradeProgress getGradeProgress(int orderIndex, Long learnerId) {
        // 1. Kiểm tra sự tồn tại của học viên và khối
        if (!learnerProfileRepo.existsById(learnerId)) {
            throw new NotFoundException("LearnerProfile with id " + learnerId);
        }
        GradeLevel gradeLevel = gradeLevelRepo.findByOrderIndex(orderIndex)
                .orElseThrow(() -> new NotFoundException("GradeLevel with orderIndex " + orderIndex));

        // 2. Lấy tất cả bài học (Lessons) thuộc khối này
        List<Lesson> lessons = lessonRepository.findByGradeLevel_IdOrderByOrderIndexAsc(gradeLevel.getId());

        GradeProgress response = new GradeProgress();
        response.setOrderIndex(orderIndex);
        response.setListLessons(new ArrayList<>());


        if (lessons.isEmpty()) {
            // Nếu khối này không có bài học, trả về DTO rỗng
            response.setLessonsLearned(0);
            response.setRewardScore(0);
            return response;
        }

        // 3. Lấy tổng điểm thưởng (RewardScore)
        Long totalReward = gameAttemptRepo.sumRewardCountByGradeOrderIndexAndLearner(orderIndex, learnerId);
        response.setRewardScore(totalReward != null ? totalReward.intValue() : 0);

        //tính sao đat đươc dưựa tren totalReward
        int stars = 0;
        int totalRewardInt = (totalReward != null) ? totalReward.intValue() : 0;
        if (totalRewardInt >= 200) {
            stars = 5;
        } else if (totalRewardInt >= 150) {
            stars = 4;
        } else if (totalRewardInt >= 100) {
            stars = 3;
        } else if (totalRewardInt >= 75) {
            stars = 2;
        } else if (totalRewardInt >= 50) {
            stars = 1;
        }
        response.setStarsArchived(stars);


        // --- Bắt đầu tối ưu hóa việc lấy dữ liệu ---
        List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();

        // 4. (Batch Fetch) Lấy tất cả LessonProgress của học viên cho các bài học này
        Map<Long, LessonProgress> lessonProgressMap = lessonProgressRepo
                .findByLearnerProfile_IdAndLesson_IdIn(learnerId, lessonIds)
                .stream()
                .collect(Collectors.toMap(lp -> lp.getLesson().getId(), lp -> lp));

        // 5. (Batch Fetch) Lấy điểm test gần nhất cho các bài học này
        List<Object[]> latestScoresRows = testAttemptRepo.findLatestTestScoresNative(learnerId, lessonIds);
        Map<Long, Double> lastTestScoreMap = new HashMap<>();
        for (Object[] row : latestScoresRows) {
            Long lessonId = ((Number) row[0]).longValue();
            Double score = ((Number) row[1]).doubleValue();
            lastTestScoreMap.put(lessonId, score);
        }
        // --- Kết thúc tối ưu hóa ---

        // 6. Xử lý và tổng hợp dữ liệu cho từng bài học
        int lessonsCompletedCount = 0;
        List<LessonOfGradeProgress> lessonProgressList = new ArrayList<>();

        for (Lesson lesson : lessons) {
            LessonOfGradeProgress lessonDTO = new LessonOfGradeProgress();
            lessonDTO.setLessonId(lesson.getId());
            lessonDTO.setUnitName(lesson.getUnitName());
            lessonDTO.setLessonName(lesson.getLessonName());

            // Lấy tiến độ bài học từ Map (hoặc 0 nếu chưa học)
            LessonProgress progress = lessonProgressMap.get(lesson.getId());
            int percent = (progress != null) ? (int) Math.round(progress.getPercentComplete()) : 0;
            lessonDTO.setLessonProgress(percent);

            // Nếu hoàn thành 100%, tăng biến đếm
            if (percent >= 100) {
                lessonsCompletedCount++;
            }

            // Lấy điểm test từ Map (hoặc 0 nếu chưa làm)
            double score = lastTestScoreMap.getOrDefault(lesson.getId(), 0.0);
            lessonDTO.setLastTestScore(score);

            lessonProgressList.add(lessonDTO);
        }

        // 7. Hoàn thiện DTO tổng
        response.setLessonsLearned(lessonsCompletedCount);
        response.setListLessons(lessonProgressList);

        return response;
    }

}
