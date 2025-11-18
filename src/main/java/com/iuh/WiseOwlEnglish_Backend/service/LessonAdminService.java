package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLessonReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.CreateLessonRes;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class LessonAdminService {
    private final LessonRepository lessonRepository;

    @Transactional
    public CreateLessonRes createLesson(CreateLessonReq req){
        Lesson lesson = toEntity(req);
        Lesson createdLesson = lessonRepository.save(lesson);
        CreateLessonRes res = toDTO(createdLesson);
        return res;
    }

    private CreateLessonRes toDTO(Lesson lesson){
        CreateLessonRes res = new CreateLessonRes();
        res.setId(lesson.getId());
        res.setUnitNumber(lesson.getUnitName());
        res.setUnitName(lesson.getLessonName());
        res.setOrderIndex(lesson.getOrderIndex());
        res.setActive(lesson.isActive());
        res.setGradeLevelId(lesson.getGradeLevel().getId());
        res.setUrlMascot(lesson.getMascot());
        res.setUpdatedAt(lesson.getUpdatedAt());
        return res;
    }

    private Lesson toEntity(CreateLessonReq req){
        Lesson lesson = new Lesson();
        lesson.setUnitName(req.getUnitNumber());
        lesson.setUnitName(req.getUnitName());
        lesson.setOrderIndex(req.getOrderIndex());
        lesson.setActive(req.isActive());
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());
        lesson.setMascot(req.getUrlMascot());
        return lesson;
    }
}
