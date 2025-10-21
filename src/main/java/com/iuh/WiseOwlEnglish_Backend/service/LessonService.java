package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonBriefRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonByClassRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonDTORS;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LessonsByAgeRes;
import com.iuh.WiseOwlEnglish_Backend.enums.ProgressStatus;
import com.iuh.WiseOwlEnglish_Backend.mapper.LessonMapper;
import com.iuh.WiseOwlEnglish_Backend.model.GradeLevel;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerProfile;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final LessonMapper lessonMapper;
    private final LearnerGradeProgressRepository grogressRepo;
    private final LearnerProfileRepository learnerProfileRepo;
    private final GradeLevelRepository gradeLevelRepo;
    private final LessonRepository lessonRepo;
    private final LessonProgressRepository lessonProgressRepo;


    public List<LessonDTORS> getAllActiveByGradeLevelId(Long gradeLevelId) {
        return lessonRepository.findAllByGradeLevel_IdAndActiveTrueOrderByOrderIndexAsc(gradeLevelId)
                .stream()
                .map(lessonMapper::toDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public LessonByClassRes getLessonsForProfile(Long learnerProfileId) {

        Optional<Long> gradeId = grogressRepo.findGradeLevelIdByLearnerAndStatusAndPrimaryTrue(learnerProfileId, ProgressStatus.IN_PROGRESS);

        // 3) Danh sách lesson theo grade
        List<Lesson> lessons = lessonRepo.findByGradeLevel_IdOrderByOrderIndexAsc(gradeId.orElse(1L));

        // 4) Lấy tiến độ theo batch
        List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();
        Map<Long, Integer> percentMap = lessonProgressRepo
                .findByLearnerProfile_IdAndLesson_IdIn(learnerProfileId, lessonIds)
                .stream()
                .collect(Collectors.toMap(
                        lp -> lp.getLesson().getId(),
                        lp -> (int) Math.round(lp.getPercentComplete()), // entity của bạn để double 0..100
                        (a, b) -> a
                ));

        // 5) Map DTO với rule trạng thái: 100 => COMPLETE, else ACTIVE
        List<LessonBriefRes> items = new ArrayList<>(lessons.size());
        for (Lesson l : lessons) {
            int pct = Optional.ofNullable(percentMap.get(l.getId())).orElse(0);
            pct = Math.max(0, Math.min(100, pct));
            String status = (pct >= 100) ? "COMPLETE" : "ACTIVE";
            LessonBriefRes lessonBriefRes = new LessonBriefRes();
            lessonBriefRes.setId(l.getId());
            lessonBriefRes.setUnitName(l.getUnitName());
            lessonBriefRes.setLessonName(l.getLessonName());
            lessonBriefRes.setOrderIndex(l.getOrderIndex());
            lessonBriefRes.setPercentComplete(pct);
            lessonBriefRes.setStatus(status);
            items.add(lessonBriefRes);
        }

        GradeLevel g = gradeLevelRepo.findById(gradeId.orElse(1L)).orElse(null);

        LessonByClassRes lessonRes = new LessonByClassRes();
        lessonRes.setProfileId(learnerProfileId);
        lessonRes.setGradeLevelId(g.getId());
        lessonRes.setGradeName(g.getGradeName());
        lessonRes.setGradeOrderIndex(g.getOrderIndex());
        lessonRes.setLessons(items);
        return lessonRes;
    }






}
