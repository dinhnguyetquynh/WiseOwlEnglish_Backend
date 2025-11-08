package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonBriefRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonsByAgeRes;
import com.iuh.WiseOwlEnglish_Backend.model.GradeLevel;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerProfile;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonQueryService {
//    private final LearnerProfileRepository learnerProfileRepo;
//    private final GradeLevelRepository gradeLevelRepo;
//    private final LessonRepository lessonRepo;
//    private final LessonProgressRepository lessonProgressRepo;
//
//    @Transactional(readOnly = true)
//    public LessonsByAgeRes getLessonsForProfileByAge(Long learnerProfileId) {
//        LearnerProfile p = learnerProfileRepo.findById(learnerProfileId)
//                .orElseThrow(() -> new NoSuchElementException("LearnerProfile not found: " + learnerProfileId));
//
//        // 1) Tuổi & grade
//        Integer age = Optional.ofNullable(p.getAge()).orElse(6);
//        int gradeOrder = mapAgeToGrade(age);
//
//        // 2) GradeLevel
//        GradeLevel g = gradeLevelRepo.findByOrderIndex(gradeOrder)
//                .orElseThrow(() -> new NoSuchElementException("GradeLevel not found for order " + gradeOrder));
//
//        // 3) Danh sách lesson theo grade
//        List<Lesson> lessons = lessonRepo.findByGradeLevel_IdOrderByOrderIndexAsc(g.getId());
//
//        // 4) Lấy tiến độ theo batch
//        List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();
//        Map<Long, Integer> percentMap = lessonProgressRepo
//                .findByLearnerProfile_IdAndLesson_IdIn(learnerProfileId, lessonIds)
//                .stream()
//                .collect(Collectors.toMap(
//                        lp -> lp.getLesson().getId(),
//                        lp -> (int) Math.round(lp.getPercentComplete()), // entity của bạn để double 0..100
//                        (a, b) -> a
//                ));
//
//        // 5) Map DTO với rule trạng thái: 100 => COMPLETE, else ACTIVE
//        List<LessonBriefRes> items = new ArrayList<>(lessons.size());
//        for (Lesson l : lessons) {
//            int pct = Optional.ofNullable(percentMap.get(l.getId())).orElse(0);
//            pct = Math.max(0, Math.min(100, pct));
//            String status = (pct >= 100) ? "COMPLETE" : "ACTIVE";
//            LessonBriefRes lessonBriefRes = new LessonBriefRes();
//            lessonBriefRes.setId(l.getId());
//            lessonBriefRes.setUnitName(l.getUnitName());
//            lessonBriefRes.setLessonName(l.getLessonName());
//            lessonBriefRes.setOrderIndex(l.getOrderIndex());
//            lessonBriefRes.setPercentComplete(pct);
//            lessonBriefRes.setStatus(status);
//            items.add(lessonBriefRes);
//        }
//
//
//        LessonsByAgeRes lessonsByAgeRes = new LessonsByAgeRes();
//        lessonsByAgeRes.setProfileId(learnerProfileId);
//        lessonsByAgeRes.setAge(age);
//        lessonsByAgeRes.setGradeLevelId(g.getId());
//        lessonsByAgeRes.setGradeName(g.getGradeName());
//        lessonsByAgeRes.setGradeOrderIndex(g.getOrderIndex());
//        lessonsByAgeRes.setLessons(items);
//        return lessonsByAgeRes;
//    }
//
//    private int mapAgeToGrade(int age) {
//        if (age <= 6) return 1;
//        if (age == 7) return 2;
//        if (age == 8) return 3;
//        if (age == 9) return 4;
//        return 5; // 10+
//    }
    private final VocabularyRepository vocabularyRepo;
    private final SentenceRepository sentenceRepo;
    private final GameQuestionRepository gameQuestionRepo; // ✅ DÙNG REPO NÀY
    private final TestQuestionRepository testQuestionRepo; // ✅ DÙNG REPO NÀY

    @Cacheable(value = "lessonTotals", key = "#lessonId + '_vocab'")
    public long getTotalVocab(Long lessonId) {
        return vocabularyRepo.countByLessonVocabulary_IdAndIsForLearning(lessonId, true);
    }

    @Cacheable(value = "lessonTotals", key = "#lessonId + '_sentence'")
    public long getTotalSentences(Long lessonId) {
        return sentenceRepo.countByLessonSentence_IdAndIsForLearning(lessonId, true);
    }

    @Cacheable(value = "lessonTotals", key = "#lessonId + '_gamequestion'")
    public long getTotalGameQuestion(Long lessonId) {
        return gameQuestionRepo.countByLessonId(lessonId);
    }

    @Cacheable(value = "lessonTotals", key = "#lessonId + '_testquestion'")
    public long getTotalTestQuestion(Long lessonId) {
        return testQuestionRepo.countByLessonId(lessonId);
    }
}
