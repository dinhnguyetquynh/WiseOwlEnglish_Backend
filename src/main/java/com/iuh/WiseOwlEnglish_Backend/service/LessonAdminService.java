package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLessonReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.UpdateLessonRequest;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.CreateLessonRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonRes;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
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
    private final LessonProgressRepository lessonProgressRepo;
    private final GameRepository gameRepository;
    // Các repo khác để Soft Delete (Vocabulary, Sentence, Test...)
    private final VocabularyRepository vocabularyRepository;
    private final SentenceRepository sentenceRepository;
    private final TestRepository testRepository;

    @Transactional
    public CreateLessonRes createLesson(CreateLessonReq req){
        // 1. Tính toán orderIndex mới tự động
        // Lấy index lớn nhất hiện tại của Grade này
        Integer currentMaxIndex = lessonRepository.findMaxOrderIndexByGradeLevelId(req.getGradeLevelId());
        // Index mới sẽ là max + 1
        int newOrderIndex = currentMaxIndex + 1;
        Lesson lesson = toEntity(req);
        lesson.setOrderIndex(newOrderIndex);
        lesson.setActive(false);
        Lesson createdLesson = lessonRepository.save(lesson);
        CreateLessonRes res = toDTO(createdLesson);
        return res;
    }
    private Lesson toEntity(CreateLessonReq req){
        Lesson lesson = new Lesson();
        lesson.setUnitName(req.getUnitNumber());
        lesson.setLessonName(req.getUnitName());
//        lesson.setOrderIndex(req.getOrderIndex());
//        lesson.setActive(false);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());
        lesson.setMascot(req.getUrlMascot());

        GradeLevel gradeLevel = gradeLevelRepository.findById(req.getGradeLevelId())
                .orElseThrow(()-> new NotFoundException("Khong tim thay grade level :" + req.getGradeLevelId()));
        lesson.setGradeLevel(gradeLevel);
        return lesson;
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



    public List<LessonRes> getListLessonByGradeId(long gradeId){
        List<Lesson> lessonList = lessonRepository.findByGradeLevel_IdAndDeletedAtIsNullOrderByOrderIndexAsc(gradeId);
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
    @Transactional
    public void deleteLesson(Long lessonId) {
        // 1. Tìm bài học
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: " + lessonId));

        // 2. Kiểm tra điều kiện
        boolean hasLearners = lessonProgressRepo.existsByLesson_Id(lessonId);
        boolean isActive = lesson.isActive();

        // 3. Phân nhánh xử lý
        if (!isActive && !hasLearners) {
            // === TRƯỜNG HỢP 1: XOÁ CỨNG (HARD DELETE) ===
            // Bài chưa active và chưa ai học -> Rác -> Xoá vĩnh viễn khỏi DB
            performHardDelete(lesson);
        } else {
            // === TRƯỜNG HỢP 2: XOÁ MỀM (SOFT DELETE) ===
            // Bài đang active hoặc đã có người học -> Ẩn đi để bảo toàn dữ liệu
            performSoftDelete(lesson);
        }
    }

    // --- Hàm hỗ trợ Xoá Cứng ---
    private void performHardDelete(Lesson lesson) {
        // Trong Model Lesson.java, Vocabulary, Sentence, Test đã được cấu hình CascadeType.ALL + orphanRemoval = true.
        // Do đó, khi xoá Lesson, JPA sẽ tự động xoá Vocab, Sentence, Test liên quan.

        // TUY NHIÊN: Model Lesson hiện tại KHÔNG có liên kết @OneToMany tới Game.
        // Vì vậy cần xoá thủ công Game trước để tránh lỗi khoá ngoại (Foreign Key).
        List<Game> games = gameRepository.findByLesson_IdAndDeletedAtIsNull(lesson.getId());
        gameRepository.deleteAll(games);

        // Sau đó xoá Lesson (Vocab, Sentence, Test sẽ tự bay màu theo)
        lessonRepository.delete(lesson);

        System.out.println("Đã HARD DELETE bài học ID: " + lesson.getId());
    }

    // --- Hàm hỗ trợ Xoá Mềm (Logic cũ đã làm) ---
    private void performSoftDelete(Lesson lesson) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Ẩn Lesson
        lesson.setDeletedAt(now);
        lesson.setActive(false);
        lessonRepository.save(lesson);

        // 2. Ẩn các thành phần con (Vocabulary, Sentence, Game)
        // (Lưu ý: Chỉ ẩn những cái chưa bị ẩn)

        // Vocab
        List<Vocabulary> vocabs = vocabularyRepository.findByLessonVocabulary_Id(lesson.getId());
        for (Vocabulary v : vocabs) {
            if (v.getDeletedAt() == null) {
                v.setDeletedAt(now);
                vocabularyRepository.save(v);
            }
        }

        // Sentence
        List<Sentence> sentences = sentenceRepository.findByLessonSentence_Id(lesson.getId());
        for (Sentence s : sentences) {
            if (s.getDeletedAt() == null) {
                s.setDeletedAt(now);
                sentenceRepository.save(s);
            }
        }

        // Game
        List<Game> games = gameRepository.findByLesson_IdAndDeletedAtIsNull(lesson.getId());
        for (Game g : games) {
            if (g.getDeletedAt() == null) {
                g.setDeletedAt(now);
                g.setActive(false); // Tắt active của game luôn
                gameRepository.save(g);
            }
        }

        // Test (Test chưa có deletedAt nên dùng active và đổi tên)
        List<Test> tests = testRepository.findByLessonTest_Id(lesson.getId());
        for (Test t : tests) {
//            if (Boolean.TRUE.equals(t.getActive())) {
//                t.setActive(false);
//                t.setTitle("[DELETED] " + t.getTitle());
//                t.setUpdatedAt(now);
//                testRepository.save(t);
//            }
            if(t.getDeletedAt()==null){
                t.setDeletedAt(now);
                t.setActive(false);
                testRepository.save(t);
            }
        }

        System.out.println("Đã SOFT DELETE bài học ID: " + lesson.getId());
    }

    //UPDATE ACTIVE CUA BAI HOC
    @Transactional
    public CreateLessonRes updateLessonActiveStatus(Long lessonId, boolean isActive) {
        // 1. Tìm bài học, nếu không thấy thì báo lỗi
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        // 2. Cập nhật trạng thái
        lesson.setActive(isActive);

        // 3. Cập nhật thời gian chỉnh sửa (quan trọng để tracking)
        lesson.setUpdatedAt(LocalDateTime.now());

        // 4. Lưu và trả về kết quả
        Lesson updatedLesson = lessonRepository.save(lesson);
        return toDTO(updatedLesson);
    }

    public LessonRes updateLesson(Long id, UpdateLessonRequest request) {
        // 1. Tìm Lesson theo ID
        Lesson existingLesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lesson với ID: " + id));

        // 2. Cập nhật các trường thông tin (Chỉ update nếu dữ liệu gửi lên khác null hoặc rỗng nếu cần)
        if (request.getUnitName() != null && !request.getUnitName().isEmpty()) {
            existingLesson.setUnitName(request.getUnitName());
        }

        if (request.getLessonName() != null && !request.getLessonName().isEmpty()) {
            existingLesson.setLessonName(request.getLessonName());
        }

        if (request.getMascot() != null) {
            existingLesson.setMascot(request.getMascot());
        }

        // 3. Hệ thống tự cập nhật trường updatedAt
        existingLesson.setUpdatedAt(LocalDateTime.now());

        // 4. Lưu xuống database
        Lesson updatedLesson = lessonRepository.save(existingLesson);
        LessonRes res = mapLessonToRes(updatedLesson);
        return res;
    }

    public LessonRes mapLessonToRes(Lesson lesson){
        LessonRes res = new LessonRes();
        res.setId(lesson.getId());
        res.setUnitNumber(lesson.getUnitName());
        res.setUnitName(lesson.getLessonName());
        res.setOrderIndex(lesson.getOrderIndex());
        res.setActive(lesson.isActive());
        res.setUrlMascot(lesson.getMascot());
        res.setUpdatedAt(lesson.getUpdatedAt());
        return res;
    }

}
