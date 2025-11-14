package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.enums.ContentType;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import com.iuh.WiseOwlEnglish_Backend.enums.PromptType;
import com.iuh.WiseOwlEnglish_Backend.enums.StemType;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.IncorrectItemLogRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerProfileRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncorrectItemLogService {
    private final IncorrectItemLogRepository logRepo;
    private final LearnerProfileRepository learnerRepo;
    private final LessonRepository lessonRepo;

    /**
     * Ghi log kết quả (ĐÚNG hoặc SAI) cho 1 item (Vocab/Sentence)
     * Đây là logic "cấn trừ"
     */
    @Transactional
    public void logItemAttempt(Long learnerId, Long lessonId, ItemType itemType, Long itemRefId, boolean isCorrect) {
        if (itemRefId == null) return; // Bỏ qua nếu không có ID

        if (isCorrect) {
            // LÀM ĐÚNG: Xóa 1 bản ghi lỗi cũ nhất (trả nợ)
            logRepo.findFirstByLearnerProfile_IdAndLesson_IdAndItemTypeAndItemRefIdOrderByWrongAtAsc(
                    learnerId, lessonId, itemType, itemRefId
            ).ifPresent(logRepo::delete); // Nếu tìm thấy, xóa nó đi

        } else {
            // LÀM SAI: Thêm 1 bản ghi lỗi mới (thêm nợ)
            IncorrectItemLog logEntry = IncorrectItemLog.builder()
                    .learnerProfile(learnerRepo.getReferenceById(learnerId))
                    .lesson(lessonRepo.getReferenceById(lessonId))
                    .itemType(itemType)
                    .itemRefId(itemRefId)
                    .wrongAt(LocalDateTime.now())
                    .build();
            logRepo.save(logEntry);
        }
    }

    /**
     * Xử lý log cho câu hỏi GAME (Quét qua Question và Options)
     */
    @Transactional
    public void logGameOptions(Long learnerId, Long lessonId, GameQuestion question, List<GameOption> options, boolean isCorrect) {
        try {
            // 1. Log item từ Prompt của câu hỏi
            if (question.getPromptType() == PromptType.VOCAB) {
                logItemAttempt(learnerId, lessonId, ItemType.VOCAB, question.getPromptRefId(), isCorrect);
            } else if (question.getPromptType() == PromptType.SENTENCE) {
                logItemAttempt(learnerId, lessonId, ItemType.SENTENCE, question.getPromptRefId(), isCorrect);
            }

            // 2. Log item từ các Options
            // (Quan trọng cho game Nối từ, Chọn từ...)
            for (GameOption opt : options) {
                if (opt.getContentType() == ContentType.VOCAB && opt.getContentRefId() != null) {
                    logItemAttempt(learnerId, lessonId, ItemType.VOCAB, opt.getContentRefId(), isCorrect);
                } else if (opt.getContentType() == ContentType.SENTENCE && opt.getContentRefId() != null) {
                    logItemAttempt(learnerId, lessonId, ItemType.SENTENCE, opt.getContentRefId(), isCorrect);
                }
            }
        } catch (Exception e) {
            log.error("Failed to log game attempt: {}", e.getMessage());
        }
    }

    /**
     * Xử lý log cho câu hỏi TEST (Quét qua Question và Options)
     */
    @Transactional
    public void logTestOptions(Long learnerId, Long lessonId, TestQuestion question, List<TestOption> options, boolean isCorrect) {
        try {
            // 1. Log item từ Stem (thân câu hỏi)
            if (question.getStemType() == StemType.VOCAB) {
                logItemAttempt(learnerId, lessonId, ItemType.VOCAB, question.getStemRefId(), isCorrect);
            } else if (question.getStemType() == StemType.SENTENCE) {
                logItemAttempt(learnerId, lessonId, ItemType.SENTENCE, question.getStemRefId(), isCorrect);
            }

            // 2. Log item từ Options
            // CHỈ LOG CÁC OPTIONS LÀ ĐÁP ÁN ĐÚNG
            // Vì mục tiêu của câu hỏi là học các từ/câu này.
            for (TestOption opt : options) {
                if (opt.isCorrect()) {
                    if (opt.getContentType() == ContentType.VOCAB && opt.getContentRefId() != null) {
                        logItemAttempt(learnerId, lessonId, ItemType.VOCAB, opt.getContentRefId(), isCorrect);
                    } else if (opt.getContentType() == ContentType.SENTENCE && opt.getContentRefId() != null) {
                        logItemAttempt(learnerId, lessonId, ItemType.SENTENCE, opt.getContentRefId(), isCorrect);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to log test attempt: {}", e.getMessage());
        }
    }

}
