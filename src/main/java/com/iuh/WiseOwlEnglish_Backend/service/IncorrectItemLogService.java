package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.enums.ContentType;
import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import com.iuh.WiseOwlEnglish_Backend.enums.PromptType;
import com.iuh.WiseOwlEnglish_Backend.enums.StemType;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.IncorrectItemLogRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerProfileRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.MediaAssetRepository;
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
    private final MediaAssetRepository mediaAssetRepository;

    /**
     * Ghi log kết quả (ĐÚNG hoặc SAI) cho 1 item (Vocab/Sentence)
     * Đây là logic "cấn trừ"
     */
//    @Transactional
//    public void logItemAttempt(Long learnerId, Long lessonId, ItemType itemType, Long itemRefId, boolean isCorrect) {
//        if (itemRefId == null) return; // Bỏ qua nếu không có ID
//
//        if (isCorrect) {
//            // LÀM ĐÚNG: Xóa 1 bản ghi lỗi cũ nhất (trả nợ)
//            logRepo.findFirstByLearnerProfile_IdAndLesson_IdAndItemTypeAndItemRefIdOrderByWrongAtAsc(
//                    learnerId, lessonId, itemType, itemRefId
//            ).ifPresent(logRepo::delete); // Nếu tìm thấy, xóa nó đi
//
//        } else {
//            // LÀM SAI: Thêm 1 bản ghi lỗi mới (thêm nợ)
//            IncorrectItemLog logEntry = IncorrectItemLog.builder()
//                    .learnerProfile(learnerRepo.getReferenceById(learnerId))
//                    .lesson(lessonRepo.getReferenceById(lessonId))
//                    .itemType(itemType)
//                    .itemRefId(itemRefId)
//                    .wrongAt(LocalDateTime.now())
//                    .build();
//            logRepo.save(logEntry);
//        }
//    }

    @Transactional
    public void logItemAttempt(Long learnerId, Long lessonId, ItemType itemType, Long itemRefId, boolean isCorrect) {
        if (itemRefId == null) return; // Bỏ qua nếu không có ID

        try {
            if (isCorrect) {
                logRepo.findFirstByLearnerProfile_IdAndLesson_IdAndItemTypeAndItemRefIdOrderByWrongAtAsc(
                        learnerId, lessonId, itemType, itemRefId
                ).ifPresent(entity -> {
                    try {
                        logRepo.delete(entity);
                    } catch (Exception ex) {
                        log.error("Failed to delete old incorrect log id={} for learner={}, lesson={}", entity.getId(), learnerId, lessonId, ex);
                    }
                });

            } else {
                LearnerProfile learner = learnerRepo.findById(learnerId).orElse(null);
                Lesson lesson = (lessonId == null) ? null : lessonRepo.findById(lessonId).orElse(null);

                IncorrectItemLog logEntry = IncorrectItemLog.builder()
                        .learnerProfile(learner)
                        .lesson(lesson)
                        .itemType(itemType)
                        .itemRefId(itemRefId)
                        .wrongAt(LocalDateTime.now())
                        .build();
                logRepo.save(logEntry);
            }
        } catch (Exception e) {
            log.error("Failed to logItemAttempt learnerId={}, lessonId={}, itemType={}, itemRefId={}", learnerId, lessonId, itemType, itemRefId, e);
        }
    }

    /**
     * Xử lý log cho câu hỏi GAME (Quét qua Question và Options)
     */
    @Transactional
    public void logGameOptions(Long learnerId, Long lessonId, GameQuestion question, List<GameOption> options, boolean isCorrect) {
        try {
            // 1. Log item từ Prompt của câu hỏi
            if (question.getPromptType() == PromptType.IMAGE||question.getPromptType() ==PromptType.AUDIO) {
                MediaAsset mediaAsset = mediaAssetRepository.findById(question.getPromptRefId())
                        .orElseThrow(()-> new NotFoundException("Khong tim thay media nao co id:"+question.getPromptRefId()));
                // --- FIX BUG NPE Ở ĐÂY ---
                if (mediaAsset != null) {
                    if (mediaAsset.getVocabulary() != null && mediaAsset.getVocabulary().getId() != null) {
                        logItemAttempt(learnerId, lessonId, ItemType.VOCAB, mediaAsset.getVocabulary().getId(), isCorrect);
                    }
                    if (mediaAsset.getSentence() != null && mediaAsset.getSentence().getId() != null) {
                        logItemAttempt(learnerId, lessonId, ItemType.SENTENCE, mediaAsset.getSentence().getId(), isCorrect);
                    }
                }

            } else if (question.getPromptType() == PromptType.SENTENCE) {
                logItemAttempt(learnerId, lessonId, ItemType.SENTENCE, question.getPromptRefId(), isCorrect);
            }

            // 2. Log item từ các Options
            // (Quan trọng cho game Nối từ, Chọn từ...)
            for (GameOption opt : options) {
                if (opt.getContentType() == ContentType.VOCAB && opt.getContentRefId() != null) {
                    logItemAttempt(learnerId, lessonId, ItemType.VOCAB, opt.getContentRefId(), isCorrect);
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

            if (question.getStemType() == StemType.IMAGE||question.getStemType() == StemType.AUDIO) {
//                MediaAsset mediaAsset = mediaAssetRepository.findById(question.getStemRefId())
//                        .orElseThrow(()-> new NotFoundException("Khong tim thay media nao co id:"+question.getStemRefId()));
                if (question.getStemRefId() != null) {
                    MediaAsset mediaAsset = mediaAssetRepository.findById(question.getStemRefId()).orElse(null);

                    // --- FIX BUG NPE Ở ĐÂY ---
                    if (mediaAsset != null) {
                        if (mediaAsset.getVocabulary() != null && mediaAsset.getVocabulary().getId() != null) {
                            logItemAttempt(learnerId, lessonId, ItemType.VOCAB, mediaAsset.getVocabulary().getId(), isCorrect);
                        }
                        if (mediaAsset.getSentence() != null && mediaAsset.getSentence().getId() != null) {
                            logItemAttempt(learnerId, lessonId, ItemType.SENTENCE, mediaAsset.getSentence().getId(), isCorrect);
                        }
                    }
                }
            } else if (question.getStemType() == StemType.SENTENCE) {
                logItemAttempt(learnerId, lessonId, ItemType.SENTENCE, question.getStemRefId(), isCorrect);
            }

            // 2. Log item từ các Options
            // (Quan trọng cho game Nối từ)
            for (TestOption opt : options) {
                if (opt.getContentType() == ContentType.VOCAB && opt.getContentRefId() != null) {
                    logItemAttempt(learnerId, lessonId, ItemType.VOCAB, opt.getContentRefId(), isCorrect);
                }
            }

        } catch (Exception e) {
            log.error("Failed to log test attempt: {}", e.getMessage());
        }
    }

}
