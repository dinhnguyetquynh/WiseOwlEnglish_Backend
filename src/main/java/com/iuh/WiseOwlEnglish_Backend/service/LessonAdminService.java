package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLessonReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.CreateLessonRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonRes;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.GradeLevel;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.repository.GradeLevelRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LessonAdminService {
    private final LessonRepository lessonRepository;
    private final GradeLevelRepository gradeLevelRepository;

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
        lesson.setLessonName(req.getUnitName());
        lesson.setOrderIndex(req.getOrderIndex());
        lesson.setActive(req.isActive());
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());
        lesson.setMascot(req.getUrlMascot());

        GradeLevel gradeLevel = gradeLevelRepository.findById(req.getGradeLevelId())
                .orElseThrow(()-> new NotFoundException("Khong tim thay grade level :" + req.getGradeLevelId()));
        lesson.setGradeLevel(gradeLevel);
        return lesson;
    }

    public List<LessonRes> getListLessonByGradeId(long gradeId){
        List<Lesson> lessonList = lessonRepository.findByGradeLevel_IdOrderByOrderIndexAsc(gradeId);
        List<LessonRes> lessonResList = new ArrayList<>();
        for(Lesson lesson:lessonList){
            LessonRes res = toLessonDTO(lesson);
            lessonResList.add(res);
        }
        return lessonResList;

    }

    public LessonRes toLessonDTO(Lesson lesson){
        LessonRes lessonRes = new LessonRes();
        lessonRes.setId(lesson.getId());
        lessonRes.setOrderIndex(lesson.getOrderIndex());
        lessonRes.setActive(lesson.isActive());
        lessonRes.setUnitNumber(lesson.getUnitName());
        lessonRes.setUnitName(lesson.getLessonName());
        lessonRes.setUrlMascot(lesson.getMascot());
        lessonRes.setUpdatedAt(lesson.getUpdatedAt());
        return lessonRes;
    }


}
