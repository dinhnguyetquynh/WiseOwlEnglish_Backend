package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLessonReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateSentenceReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.SentenceUpdateReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.VocabUpdateReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.SentenceAdminRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.SentenceUpdateRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.enums.*;
import com.iuh.WiseOwlEnglish_Backend.event.LessonContentChangedEvent;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import com.iuh.WiseOwlEnglish_Backend.model.Sentence;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SentenceAdminService {
    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;
    private final MediaAssetRepository mediaAssetRepository;

    private final TransactionTemplate transactionTemplate;

    private final ContentProgressRepository contentProgressRepo;
    private final IncorrectItemLogRepository incorrectItemLogRepo;

    private final GameQuestionRepository gameQuestionRepo;
    private final GameOptionRepository gameOptionRepo;
    private final TestQuestionRepository testQuestionRepo;
    private final TestOptionRepository testOptionRepo;

    private static final int MAX_RETRY = 5;        // tăng retry nếu muốn
    private static final long RETRY_SLEEP_MS = 80; // backoff ngắn

    // Inject Publisher
    private final ApplicationEventPublisher eventPublisher;

    public List<SentenceAdminRes> getListSentence(long lessonId){
        List<Sentence> sentenceList = sentenceRepository.findByLessonSentenceIdAndDeletedAtIsNullOrderByOrderIndexAsc(lessonId);

        List<SentenceAdminRes> sentenceResList = new ArrayList<>();
        for(Sentence sentence: sentenceList){
            SentenceAdminRes res = toDTO(sentence);
            sentenceResList.add(res);
        }
        return sentenceResList;
    }

    private SentenceAdminRes toDTO(Sentence sentence){
        SentenceAdminRes res = new SentenceAdminRes();
        res.setId(sentence.getId());
        res.setOrderIndex(sentence.getOrderIndex());
        res.setSen_en(sentence.getSentence_en());
        res.setSen_vi(sentence.getSentence_vi());
        res.setForLearning(sentence.isForLearning());
        MediaAsset imgUrl = mediaAssetRepository.findBySentenceIdAndMediaType(sentence.getId(), MediaType.IMAGE);
        res.setImgUrl(imgUrl.getUrl());

        MediaAsset audioNormal = mediaAssetRepository.findBySentenceIdAndMediaTypeAndTag(sentence.getId(),MediaType.AUDIO,"normal");
        res.setAudioNormal(audioNormal.getUrl());

        MediaAsset slowNormal = mediaAssetRepository.findBySentenceIdAndMediaTypeAndTag(sentence.getId(),MediaType.AUDIO,"slow");
        res.setAudioSlow(slowNormal.getUrl());
        return res;
    }


    /**
     * Tạo Sentence: mỗi attempt sẽ chạy trong 1 transaction riêng (TransactionTemplate).
     * Nếu có lỗi DataIntegrityViolationException (ví dụ trùng unique orderIndex), sẽ retry.
     */
    @CacheEvict(value = "lessonTotals", key = "#req.lessonId + '_sentence'")
    public SentenceAdminRes createSentence(CreateSentenceReq req) {
        Lesson lesson = lessonRepository.findById(req.getLessonId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay lesson: " + req.getLessonId()));

        int attempt = 0;
        while (true) {
            attempt++;
            try {
                // chạy một attempt trong transaction riêng
                SentenceAdminRes res = transactionTemplate.execute(status -> {
                    // 1) Tạo Sentence và gán orderIndex = max + 1 (tính tại thời điểm này)
                    int maxOrder = sentenceRepository.findMaxOrderIndexByLessonId(lesson.getId());
                    Sentence sentence = new Sentence();
                    sentence.setOrderIndex(maxOrder + 1);
                    sentence.setSentence_en(req.getSen_en());
                    sentence.setSentence_vi(req.getSen_vn());
                    sentence.setCreatedAt(LocalDateTime.now());
                    sentence.setUpdatedAt(LocalDateTime.now());
                    sentence.setLessonSentence(lesson);
                    sentence.setForLearning(req.isForLearning());

                    Sentence created = sentenceRepository.save(sentence); // persist sentence

                    // 2) Lưu các MediaAsset (nếu có) — lưu đúng object + set sentence
                    if (req.getUrlImg() != null && !req.getUrlImg().isBlank()) {
                        MediaAsset mediaImg = new MediaAsset();
                        mediaImg.setUrl(req.getUrlImg());
                        mediaImg.setMediaType(MediaType.IMAGE);
                        mediaImg.setAltText(created.getSentence_en());
                        mediaImg.setStorageProvider("Cloudinary");
                        mediaImg.setTag("img");
                        mediaImg.setCreatedAt(LocalDateTime.now());
                        mediaImg.setUpdatedAt(LocalDateTime.now());
                        mediaImg.setSentence(created);
                        mediaAssetRepository.save(mediaImg);
                    }

                    if (req.getUrlAudioNormal() != null && !req.getUrlAudioNormal().isBlank()) {
                        MediaAsset mediaAudioNormal = new MediaAsset();
                        mediaAudioNormal.setUrl(req.getUrlAudioNormal());
                        mediaAudioNormal.setMediaType(MediaType.AUDIO);
                        mediaAudioNormal.setAltText(created.getSentence_en());
                        mediaAudioNormal.setDurationSec(req.getDurationSecNormal());
                        mediaAudioNormal.setStorageProvider("Cloudinary");
                        mediaAudioNormal.setTag("normal");
                        mediaAudioNormal.setCreatedAt(LocalDateTime.now());
                        mediaAudioNormal.setUpdatedAt(LocalDateTime.now());
                        mediaAudioNormal.setSentence(created);
                        mediaAssetRepository.save(mediaAudioNormal);
                    }

                    if (req.getUrlAudioSlow() != null && !req.getUrlAudioSlow().isBlank()) {
                        MediaAsset mediaAudioSlow = new MediaAsset();
                        mediaAudioSlow.setUrl(req.getUrlAudioSlow());
                        mediaAudioSlow.setMediaType(MediaType.AUDIO);
                        mediaAudioSlow.setAltText(created.getSentence_en());
                        mediaAudioSlow.setDurationSec(req.getDurationSecSlow());
                        mediaAudioSlow.setStorageProvider("Cloudinary");
                        mediaAudioSlow.setTag("slow");
                        mediaAudioSlow.setCreatedAt(LocalDateTime.now());
                        mediaAudioSlow.setUpdatedAt(LocalDateTime.now());
                        mediaAudioSlow.setSentence(created);
                        mediaAssetRepository.save(mediaAudioSlow);
                    }

                    // 3) Build response DTO
                    SentenceAdminRes resLocal = new SentenceAdminRes();
                    resLocal.setId(created.getId());
                    resLocal.setOrderIndex(created.getOrderIndex());
                    resLocal.setSen_en(created.getSentence_en());
                    return resLocal;
                });

                if (res != null) {
                    eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
                }
                // nếu transactionTemplate.execute trả về res (không bị exception) => thành công
                return res;

            } catch (DataIntegrityViolationException dive) {
                // Thường do unique constraint (lesson_id, order_index) bị vi phạm => retry
                if (attempt >= MAX_RETRY) {
                    throw new BadRequestException("Khong tao duoc sentence sau " + MAX_RETRY + " lan thu do xung dot orderIndex");
                }
                // backoff ngắn để giảm khả năng collision ở lần retry tiếp theo
                try {
                    Thread.sleep(RETRY_SLEEP_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry sleep", ie);
                }
                // tiếp tục vòng lặp để thử lại
            } catch (RuntimeException ex) {
                // Lỗi khác (ví dụ dữ liệu thiếu, validation...) -> ném ra luôn
                throw ex;
            }
        }
    }
    @Transactional
    public String deleteSentence(Long sentenceId) {
        // 1. Tìm Sentence
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Sentence với id: " + sentenceId));

        // 2. CHECK RÀNG BUỘC CẤU TRÚC (GAME & TEST)

        // Game
        if (gameQuestionRepo.existsByPromptTypeAndPromptRefIdAndDeletedAtIsNull(PromptType.SENTENCE, sentenceId)) {
            throw new BadRequestException("Không thể xóa: Câu này đang là Prompt trong Game.");
        }
        if (gameOptionRepo.existsByContentTypeAndContentRefIdAndDeletedAtIsNull(ContentType.SENTENCE, sentenceId)) {
            throw new BadRequestException("Không thể xóa: Câu này đang là Option trong Game.");
        }

        // Test
        if (testQuestionRepo.existsByStemTypeAndStemRefId(StemType.SENTENCE, sentenceId)) {
            throw new BadRequestException("Không thể xóa: Câu này đang là Stem trong Test.");
        }
        if (testOptionRepo.existsByContentTypeAndContentRefId(ContentType.SENTENCE, sentenceId)) {
            throw new BadRequestException("Không thể xóa: Câu này đang là Option trong Test.");
        }

        // 3. CHECK RÀNG BUỘC NGƯỜI HỌC
        boolean hasLearned = contentProgressRepo.existsByItemTypeAndItemRefId(ItemType.SENTENCE, sentenceId);
        boolean hasErrorLog = incorrectItemLogRepo.existsByItemTypeAndItemRefId(ItemType.SENTENCE, sentenceId);

        if (hasLearned || hasErrorLog) {
            // === XÓA MỀM ===
            LocalDateTime now = LocalDateTime.now();
            sentence.setDeletedAt(now);

            if (sentence.getMediaAssets() != null) {
                for (MediaAsset media : sentence.getMediaAssets()) {
                    if (media.getDeletedAt() == null) {
                        media.setDeletedAt(now);
                        mediaAssetRepository.save(media);
                    }
                }
            }
            sentenceRepository.save(sentence);
            eventPublisher.publishEvent(new LessonContentChangedEvent(this, sentence.getLessonSentence().getId()));
            return "Soft Deleted: Câu đã được ẩn vì có người học.";
        } else {
            // === XÓA CỨNG ===
            sentenceRepository.delete(sentence);
            eventPublisher.publishEvent(new LessonContentChangedEvent(this, sentence.getLessonSentence().getId()));
            return "Hard Deleted: Câu đã được xóa vĩnh viễn.";
        }
    }

    @Transactional(rollbackFor = Exception.class) // 1. Thêm annotation này
    public SentenceUpdateRes updateSentence(Long id, SentenceUpdateReq req) {
        // --- PHẦN 1: VALIDATION ---

        // 1. Tìm từ vựng
        Sentence sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sentence có id: " + id));

        // 2. Check Lesson Active
        Lesson lesson = sentence.getLessonSentence();
        if (lesson != null && Boolean.TRUE.equals(lesson.isActive())) {
            throw new BadRequestException("Không thể cập nhật câu khi bài học đang KÍCH HOẠT (Public). Vui lòng tắt kích hoạt bài học.");
        }

        // 3. Check Game & Test Constraints
        checkGameAndTestConstraints(sentence.getId()); // Mình gom gọn logic check vào hàm riêng cho sạch code nếu cần

        // 4. Check Ràng buộc người học
        boolean hasLearned = contentProgressRepo.existsByItemTypeAndItemRefId(ItemType.SENTENCE, sentence.getId());
        boolean hasErrorLog = incorrectItemLogRepo.existsByItemTypeAndItemRefId(ItemType.SENTENCE, sentence.getId());

        if (hasLearned || hasErrorLog) {
            throw new BadRequestException("Câu này đã có người học. Vui lòng không cập nhật.");
        }

        // --- PHẦN 2: UPDATE DATA ---

        // Cập nhật thông tin Sentence
        sentence.setSentence_en(req.getSen_en());
        sentence.setSentence_vi(req.getSen_vi());
        sentence.setUpdatedAt(LocalDateTime.now());

        // Cập nhật MediaAsset
        // Lưu ý: Nhờ @Transactional, các thay đổi setUrl dưới đây sẽ tự động được update xuống DB
        MediaAsset imgMedia = mediaAssetRepository.findBySentenceIdAndMediaType(sentence.getId(), MediaType.IMAGE);
        if (imgMedia != null) imgMedia.setUrl(req.getImgUrl()); // Nên check null để an toàn

        MediaAsset normalAudio = mediaAssetRepository.findBySentenceIdAndMediaTypeAndTag(sentence.getId(), MediaType.AUDIO, "normal");
        if (normalAudio != null) normalAudio.setUrl(req.getAudioNormal());

        MediaAsset slowAudio = mediaAssetRepository.findBySentenceIdAndMediaTypeAndTag(sentence.getId(), MediaType.AUDIO, "slow");
        if (slowAudio != null) slowAudio.setUrl(req.getAudioSlow());

        // Lưu Sentence (MediaAsset sẽ tự được lưu nhờ Dirty Checking của Transaction)
        Sentence saved = sentenceRepository.save(sentence);

        // --- PHẦN 3: RESPONSE ---

        SentenceUpdateRes res = new SentenceUpdateRes();
        res.setId(saved.getId());
        res.setForLearning(saved.isForLearning());
        res.setOrderIndex(saved.getOrderIndex());
        res.setSentence_en(saved.getSentence_en());
        res.setSentence_vi(saved.getSentence_vi());

        // TỐI ƯU: Sử dụng luôn biến imgMedia, normalAudio đã lấy ở trên
        // Không cần gọi lại repository findBy... nữa để tiết kiệm tài nguyên DB
        res.setImgUrl(imgMedia != null ? imgMedia.getUrl() : null);
        res.setAudioNormal(normalAudio != null ? normalAudio.getUrl() : null);
        res.setAudioSlow(slowAudio != null ? slowAudio.getUrl() : null);

        return res;
    }

    // Hàm phụ để code chính gọn hơn (Optional)
    private void checkGameAndTestConstraints(Long sentenceId) {
        if (gameQuestionRepo.existsByPromptTypeAndPromptRefIdAndDeletedAtIsNull(PromptType.SENTENCE, sentenceId)) {
            throw new BadRequestException("Không thể cập nhật: Câu này đang được dùng trong Game.");
        }
        if (gameOptionRepo.existsByContentTypeAndContentRefIdAndDeletedAtIsNull(ContentType.SENTENCE, sentenceId)) {
            throw new BadRequestException("Không thể cập nhật: Câu này đang là Option trong Game.");
        }
        if (testQuestionRepo.existsByStemTypeAndStemRefId(StemType.SENTENCE, sentenceId)) {
            throw new BadRequestException("Không thể cập nhật: Câu này đang là Stem trong Test.");
        }
        if (testOptionRepo.existsByContentTypeAndContentRefId(ContentType.SENTENCE, sentenceId)) {
            throw new BadRequestException("Không thể cập nhật: Câu này đang là Option trong Test.");
        }
    }
}
