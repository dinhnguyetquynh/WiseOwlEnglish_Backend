package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.enums.ProgressStatus;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.mapper.LessonMapper;
import com.iuh.WiseOwlEnglish_Backend.model.*;
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
    private final GameRepository gameRepository;

    public List<LessonDTORS> getAllActiveByGradeLevelId(Long gradeLevelId) {
        // SỬA: ActiveTrue + DeletedAtIsNull
        return lessonRepository.findAllByGradeLevel_IdAndActiveTrueAndDeletedAtIsNullOrderByOrderIndexAsc(gradeLevelId)
                .stream()
                .map(lessonMapper::toDTO)
                .toList();
    }
    // Inject Calculator Service để tính toán lại
    private final LessonCalculatorService lessonCalculatorService;

    @Transactional(readOnly = true)
    public LessonByClassRes getLessonsForProfile(Long learnerProfileId) {

        Optional<Long> gradeId = grogressRepo.findGradeLevelIdByLearnerAndStatusAndPrimaryTrue(learnerProfileId, ProgressStatus.IN_PROGRESS);

        // SỬA: Chỉ lấy bài học chưa bị xoá va dang active để tính toán hiển thị
        List<Lesson> lessons = lessonRepo.findAllByGradeLevel_IdAndActiveTrueAndDeletedAtIsNullOrderByOrderIndexAsc(gradeId.orElse(1L));
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
            // --- FIX: TÍNH TOÁN LẠI TIẾN ĐỘ THAY VÌ LẤY TỪ DB ---
            // Điều này đảm bảo nếu Admin thêm bài mới, mẫu số tăng lên -> % giảm xuống
//            double calculatedPercent = lessonCalculatorService.calculateCurrentProgress(learnerProfileId, l.getId());
//            int pct = (int) Math.round(calculatedPercent);
            pct = Math.max(0, Math.min(100, pct)); // Kẹp trong khoảng 0-100
            String status = (pct >= 100) ? "COMPLETE" : "ACTIVE";
            LessonBriefRes lessonBriefRes = new LessonBriefRes();
            lessonBriefRes.setId(l.getId());
            lessonBriefRes.setUnitName(l.getUnitName());
            lessonBriefRes.setLessonName(l.getLessonName());
            lessonBriefRes.setOrderIndex(l.getOrderIndex());
            lessonBriefRes.setPercentComplete(pct);
            lessonBriefRes.setStatus(status);
            lessonBriefRes.setMascot(l.getMascot());
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



    //FUNCTION FOR ADMIN

    public List<LessonByGradeRes> getListLessonByGrade(long gradeId){
        // SỬA: Thêm DeletedAtIsNull
        List<Lesson> lessonList = lessonRepository. findAllByGradeLevel_IdAndActiveTrueAndDeletedAtIsNullOrderByOrderIndexAsc(gradeId);
        List<LessonByGradeRes> lessonByGradeRes = new ArrayList<>();
        for(Lesson lesson:lessonList){
            LessonByGradeRes res = new LessonByGradeRes();
            res.setId(lesson.getId());
            res.setUnitName(lesson.getUnitName());
            res.setLessonName(lesson.getLessonName());
            res.setActive(lesson.isActive());
            res.setMascot(lesson.getMascot());
            res.setUpdatedAt(lesson.getUpdatedAt());
            res.setOrderIndex(lesson.getOrderIndex());
            lessonByGradeRes.add(res);
        }
        return lessonByGradeRes;
    }



    /**
     * LẤY DANH SÁCH BÀI HỌC VÀ TIẾN ĐỘ CỦA HỌC VIÊN CHO MỘT LỚP CỤ THỂ
     */
    @Transactional(readOnly = true)
    public LessonByClassRes getLessonsByGradeForProfile(Long learnerProfileId, int gradeOrderIndex) {

        // 1. Lấy GradeLevel bằng orderIndex
        GradeLevel g = gradeLevelRepo.findByOrderIndex(gradeOrderIndex)
                .orElseThrow(() -> new NotFoundException("GradeLevel with orderIndex " + gradeOrderIndex));

        // 2. KIỂM TRA TRẠNG THÁI CỦA KHỐI LỚP (Grade) NÀY
        LearnerGradeProgress lgp = grogressRepo.findByLearnerProfile_IdAndGradeLevel_Id(learnerProfileId, g.getId())
                .orElse(null); // Tìm trạng thái chung

        // Mặc định là KHÓA nếu không tìm thấy bản ghi
        ProgressStatus gradeStatus = (lgp != null) ? lgp.getStatus() : ProgressStatus.LOCKED;

        // 3. Danh sách lesson theo grade
        List<Lesson> lessons = lessonRepo. findAllByGradeLevel_IdAndActiveTrueAndDeletedAtIsNullOrderByOrderIndexAsc(g.getId());

        // 4. Map DTO
        List<LessonBriefRes> items = new ArrayList<>(lessons.size());

        // 5. LOGIC PHÂN NHÁNH DỰA TRÊN TRẠNG THÁI LỚP
        if (gradeStatus == ProgressStatus.LOCKED) {
            // NẾU CẢ LỚP BỊ KHÓA
            for (Lesson l : lessons) {
                LessonBriefRes lessonBriefRes = new LessonBriefRes();
                lessonBriefRes.setId(l.getId());
                lessonBriefRes.setUnitName(l.getUnitName());
                lessonBriefRes.setLessonName(l.getLessonName());
                lessonBriefRes.setOrderIndex(l.getOrderIndex());
                lessonBriefRes.setPercentComplete(0);     // 0%
                lessonBriefRes.setStatus("LOCKED");       // Ép trạng thái bài học là LOCKED
                lessonBriefRes.setMascot(l.getMascot());
                items.add(lessonBriefRes);
            }
        } else {
            // NẾU LỚP LÀ IN_PROGRESS hoặc COMPLETED (Logic cũ)
            List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();
            Map<Long, Integer> percentMap = lessonProgressRepo
                    .findByLearnerProfile_IdAndLesson_IdIn(learnerProfileId, lessonIds)
                    .stream()
                    .collect(Collectors.toMap(
                            lp -> lp.getLesson().getId(),
                            lp -> (int) Math.round(lp.getPercentComplete()),
                            (a, b) -> a
                    ));

            for (Lesson l : lessons) {
                int pct = percentMap.getOrDefault(l.getId(), 0);
                pct = Math.max(0, Math.min(100, pct));
                // --- FIX: TÍNH TOÁN LẠI TIẾN ĐỘ ---
//                double calculatedPercent = lessonCalculatorService.calculateCurrentProgress(learnerProfileId, l.getId());
//                int pct = (int) Math.round(calculatedPercent);
                pct = Math.max(0, Math.min(100, pct));
                String status = (pct >= 100) ? "COMPLETE" : "ACTIVE";
                LessonBriefRes lessonBriefRes = new LessonBriefRes();
                lessonBriefRes.setId(l.getId());
                lessonBriefRes.setUnitName(l.getUnitName());
                lessonBriefRes.setLessonName(l.getLessonName());
                lessonBriefRes.setOrderIndex(l.getOrderIndex());
                lessonBriefRes.setPercentComplete(pct);
                lessonBriefRes.setStatus(status); // Status là ACTIVE hoặc COMPLETE
                lessonBriefRes.setMascot(l.getMascot());
                items.add(lessonBriefRes);
            }
        }

        // 6. Trả về DTO
        LessonByClassRes lessonRes = new LessonByClassRes();
        lessonRes.setProfileId(learnerProfileId);
        lessonRes.setGradeLevelId(g.getId());
        lessonRes.setGradeName(g.getGradeName());
        lessonRes.setGradeOrderIndex(g.getOrderIndex());
        lessonRes.setLessons(items);
        return lessonRes;
    }

    public LessonByClassRes getLessonForGuest(long gradeId){
        GradeLevel gradeLevel = gradeLevelRepo.findById(gradeId)
                .orElseThrow(()-> new NotFoundException("Khong tim thay gradeLevel co id:"+gradeId));
        List<Lesson> lessonList = lessonRepository.findActiveLessonsByGrade(gradeId);

        LessonByClassRes res = new LessonByClassRes();
        res.setGradeLevelId(gradeLevel.getId());
        res.setGradeName(gradeLevel.getGradeName());
        res.setGradeOrderIndex(gradeLevel.getOrderIndex());

        List<LessonBriefRes> lessonBriefResList = new ArrayList<>();
        for(Lesson lesson : lessonList){
            LessonBriefRes lessonBriefRes = toDTO(lesson);
            lessonBriefResList.add(lessonBriefRes);
        }
        res.setLessons(lessonBriefResList);
        return res;
    }

    private LessonBriefRes toDTO(Lesson lesson){
        LessonBriefRes res = new LessonBriefRes();
        res.setId(lesson.getId());
        res.setUnitName(lesson.getUnitName());
        res.setLessonName(lesson.getLessonName());
        res.setOrderIndex(lesson.getOrderIndex());
        res.setMascot(lesson.getMascot());
        return res;

    }



}
