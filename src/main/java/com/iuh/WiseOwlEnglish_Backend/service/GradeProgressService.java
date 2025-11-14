package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import com.iuh.WiseOwlEnglish_Backend.enums.TestAttemptStatus;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    private final TestRepository testRepo;
    private final VocabularyRepository vocabRepo;
    private final SentenceRepository sentenceRepo;
    private final IncorrectItemLogRepository incorrectItemLogRepo;



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

    @Transactional(readOnly = true)
    public LessonProgressDetailRes getLessonProgressDetail(Long learnerId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found"));

        LessonProgressDetailRes res = new LessonProgressDetailRes();
        res.setLessonId(lessonId);
        res.setUnitName(lesson.getUnitName());
        res.setLessonName(lesson.getLessonName());

        res.setTestHistories(getTestHistoryForLesson(learnerId, lessonId));
        res.setIncorrectVocabularies(getIncorrectVocabularies(learnerId, lessonId));
        res.setIncorrectSentences(getIncorrectSentences(learnerId, lessonId));

        return res;
    }
    // 👇 HÀM HELPER (Giữ nguyên)
    private List<TestAttemptHistoryRes> getTestHistoryForLesson(Long learnerId, Long lessonId) {
        List<Test> testsInLesson = testRepo.findByLessonTest_Id(lessonId);
        List<TestAttemptHistoryRes> histories = new ArrayList<>();

        for (Test test : testsInLesson) {
            List<TestAttempt> attempts = testAttemptRepo
                    .findByLearnerProfile_IdAndTest_IdAndStatusOrderByFinishedAtAsc(
                            learnerId, test.getId(), TestAttemptStatus.FINISHED);

            if (attempts.isEmpty()) continue;

            List<TestAttemptHistoryRes.AttemptScore> scores = attempts.stream()
                    .map(att -> new TestAttemptHistoryRes.AttemptScore(
                            att.getId(),
                            att.getScore(),
                            att.getFinishedAt()
                    ))
                    .toList();

            TestAttemptHistoryRes testHistory = new TestAttemptHistoryRes();
            testHistory.setTestId(test.getId());
            testHistory.setTestTitle(test.getTitle());
            testHistory.setAttempts(scores);
            histories.add(testHistory);
        }
        return histories;
    }

    // 👇 HÀM HELPER (Logic truy vấn mới)
    private List<IncorrectItemRes> getIncorrectVocabularies(Long learnerId, Long lessonId) {
        List<IncorrectItemCountDTO> wrongCounts = incorrectItemLogRepo
                .findIncorrectItemCounts(learnerId, lessonId, ItemType.VOCAB);

        if (wrongCounts.isEmpty()) return Collections.emptyList();

        List<IncorrectItemCountDTO> top5 = wrongCounts.stream().limit(5).toList();
        Set<Long> vocabIds = top5.stream().map(IncorrectItemCountDTO::getItemRefId).collect(Collectors.toSet());
        Map<Long, Long> countMap = top5.stream().collect(Collectors.toMap(IncorrectItemCountDTO::getItemRefId, IncorrectItemCountDTO::getWrongCount));

        List<Vocabulary> vocabs = vocabRepo.findAllById(vocabIds);

        return vocabs.stream()
                .map(v -> new IncorrectItemRes(
                        v.getTerm_en(),
                        v.getTerm_vi(),
                        countMap.getOrDefault(v.getId(), 0L)
                ))
                .sorted(Comparator.comparingLong(IncorrectItemRes::getWrongCount).reversed())
                .collect(Collectors.toList());
    }

    // 👇 HÀM HELPER (Logic truy vấn mới)
    private List<IncorrectItemRes> getIncorrectSentences(Long learnerId, Long lessonId) {
        List<IncorrectItemCountDTO> wrongCounts = incorrectItemLogRepo
                .findIncorrectItemCounts(learnerId, lessonId, ItemType.SENTENCE);

        if (wrongCounts.isEmpty()) return Collections.emptyList();

        List<IncorrectItemCountDTO> top5 = wrongCounts.stream().limit(5).toList();
        Set<Long> sentenceIds = top5.stream().map(IncorrectItemCountDTO::getItemRefId).collect(Collectors.toSet());
        Map<Long, Long> countMap = top5.stream().collect(Collectors.toMap(IncorrectItemCountDTO::getItemRefId, IncorrectItemCountDTO::getWrongCount));

        List<Sentence> sentences = sentenceRepo.findAllById(sentenceIds);

        return sentences.stream()
                .map(s -> new IncorrectItemRes(
                        s.getSentence_en(),
                        s.getSentence_vi(),
                        countMap.getOrDefault(s.getId(), 0L)
                ))
                .sorted(Comparator.comparingLong(IncorrectItemRes::getWrongCount).reversed())
                .collect(Collectors.toList());
    }

}
