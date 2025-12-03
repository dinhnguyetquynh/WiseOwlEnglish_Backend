package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.CreateVocabReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.enums.*;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VocabServiceAdmin {
    private final VocabularyRepository vocabularyRepository;
    private final LessonRepository lessonRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final TransactionTemplate transactionTemplate;

    private final ContentProgressRepository contentProgressRepo;
    private final IncorrectItemLogRepository incorrectItemLogRepo;

    private final GameQuestionRepository gameQuestionRepo;
    private final GameOptionRepository gameOptionRepo;
    private final TestQuestionRepository testQuestionRepo;
    private final TestOptionRepository testOptionRepo;

    private static final int MAX_RETRY = 5;
    private static final long RETRY_SLEEP_MS = 80L;


    public List<VocabRes> getListVocab(long lessonId){
        List<Vocabulary> vocabularyList = vocabularyRepository.findByLessonVocabulary_Id(lessonId);
        List<VocabRes> vocabResList = new ArrayList<>();
        for(Vocabulary vocabulary: vocabularyList){
            VocabRes res = toDTO(vocabulary);
            vocabResList.add(res);
        }
        return vocabResList;
    }

    private VocabRes toDTO(Vocabulary vocabulary){
        VocabRes vocabRes = new VocabRes();
        vocabRes.setId(vocabulary.getId());
        vocabRes.setOrderIndex(vocabulary.getOrderIndex());
        vocabRes.setTerm_en(vocabulary.getTerm_en());
        vocabRes.setPartOfSpeech(vocabulary.getPartOfSpeech());
        return vocabRes;
    }

    public VocabRes createVocab(CreateVocabReq req) {
        // Tìm lesson ngoài vòng retry (không cần lặp nhiều lần)
        Lesson lesson = lessonRepository.findById(req.getLessonId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay lesson: " + req.getLessonId()));

        int attempt = 0;
        while (true) {
            attempt++;
            try {
                VocabRes result = transactionTemplate.execute(status -> {
                    // 1) Tính orderIndex tại thời điểm này
                    int maxOrder = vocabularyRepository.findMaxOrderIndexByLessonId(lesson.getId());
                    int nextOrder = maxOrder + 1;

                    // 2) Tạo và lưu vocabulary
                    Vocabulary vocab = new Vocabulary();
                    vocab.setTerm_en(req.getTerm_en());
                    vocab.setTerm_vi(req.getTerm_vn());
                    vocab.setPhonetic(req.getPhonetic());
                    vocab.setOrderIndex(nextOrder);                 // hệ thống tự gán
                    vocab.setPartOfSpeech(req.getPartOfSpeech());
                    vocab.setCreatedAt(LocalDateTime.now());
                    vocab.setUpdatedAt(LocalDateTime.now());
                    vocab.setLessonVocabulary(lesson);
                    vocab.setForLearning(req.isForLearning());

                    Vocabulary created = vocabularyRepository.save(vocab);

                    // 3) Lưu MediaAsset nếu có (lưu đúng entity tương ứng)
                    if (req.getUrlImg() != null && !req.getUrlImg().isBlank()) {
                        MediaAsset mediaImg = new MediaAsset();
                        mediaImg.setUrl(req.getUrlImg());
                        mediaImg.setMediaType(MediaType.IMAGE);
                        mediaImg.setAltText(created.getTerm_en());
                        mediaImg.setStorageProvider("Cloudinary");
                        mediaImg.setTag("img");
                        mediaImg.setCreatedAt(LocalDateTime.now());
                        mediaImg.setUpdatedAt(LocalDateTime.now());
                        mediaImg.setVocabulary(created);
                        mediaAssetRepository.save(mediaImg);
                    }

                    if (req.getUrlAudioNormal() != null && !req.getUrlAudioNormal().isBlank()) {
                        MediaAsset mediaAudioNormal = new MediaAsset();
                        mediaAudioNormal.setUrl(req.getUrlAudioNormal());
                        mediaAudioNormal.setMediaType(MediaType.AUDIO);
                        mediaAudioNormal.setAltText(created.getTerm_en());
                        mediaAudioNormal.setDurationSec(req.getDurationSecNormal());
                        mediaAudioNormal.setStorageProvider("Cloudinary");
                        mediaAudioNormal.setTag("normal");
                        mediaAudioNormal.setCreatedAt(LocalDateTime.now());
                        mediaAudioNormal.setUpdatedAt(LocalDateTime.now());
                        mediaAudioNormal.setVocabulary(created);
                        mediaAssetRepository.save(mediaAudioNormal);
                    }

                    if (req.getUrlAudioSlow() != null && !req.getUrlAudioSlow().isBlank()) {
                        MediaAsset mediaAudioSlow = new MediaAsset();
                        mediaAudioSlow.setUrl(req.getUrlAudioSlow());
                        mediaAudioSlow.setMediaType(MediaType.AUDIO);
                        mediaAudioSlow.setAltText(created.getTerm_en());
                        mediaAudioSlow.setDurationSec(req.getDurationSecSlow()); // đúng object
                        mediaAudioSlow.setStorageProvider("Cloudinary");
                        mediaAudioSlow.setTag("slow");
                        mediaAudioSlow.setCreatedAt(LocalDateTime.now());
                        mediaAudioSlow.setUpdatedAt(LocalDateTime.now());
                        mediaAudioSlow.setVocabulary(created);
                        mediaAssetRepository.save(mediaAudioSlow);
                    }

                    // 4) Build response DTO
                    VocabRes res = new VocabRes();
                    res.setId(created.getId());
                    res.setOrderIndex(created.getOrderIndex());
                    res.setTerm_en(created.getTerm_en());
                    res.setPhonetic(created.getPhonetic());
                    res.setPartOfSpeech(created.getPartOfSpeech());
                    return res;
                });

                // Nếu không exception => thành công
                return result;

            } catch (DataIntegrityViolationException dive) {
                // Thường là do unique constraint (lesson_id, order_index) bị vi phạm
                if (attempt >= MAX_RETRY) {
                    throw new BadRequestException("Khong tao duoc vocab sau " + MAX_RETRY + " lan thu do xung dot orderIndex");
                }
                // Sleep ngắn để giảm collision cho lần thử tiếp theo
                try {
                    Thread.sleep(RETRY_SLEEP_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry sleep", ie);
                }
                // tiếp tục vòng lặp để thử lại
            } catch (RuntimeException ex) {
                // Lỗi khác: ném ra luôn (ví dụ validation)
                throw ex;
            }
        }
    }
    @Transactional
    public String deleteVocab(Long vocabId) {
        // 1. Tìm Vocabulary
        Vocabulary vocab = vocabularyRepository.findById(vocabId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy Vocabulary với id: " + vocabId));

        // 2. [LOGIC MỚI] CHECK TRẠNG THÁI BÀI HỌC (LESSON STATUS)
        Lesson lesson = vocab.getLessonVocabulary();
        if (lesson != null && Boolean.TRUE.equals(lesson.isActive())) {
            throw new BadRequestException(
                    "Không thể xóa từ vựng khi bài học đang KÍCH HOẠT (Public). " +
                            "Vui lòng tắt kích hoạt bài học hoặc xóa toàn bộ bài học."
            );
        }

        // 3. CHECK RÀNG BUỘC CẤU TRÚC (GAME & TEST)
        // Dù bài học chưa active, vẫn phải chặn nếu từ này đã được gán vào Game/Test (để tránh lỗi config)
        if (gameQuestionRepo.existsByPromptTypeAndPromptRefIdAndDeletedAtIsNull(PromptType.VOCAB, vocabId) ||
                gameOptionRepo.existsByContentTypeAndContentRefIdAndDeletedAtIsNull(ContentType.VOCAB, vocabId)) {
            throw new BadRequestException("Không thể xóa: Từ vựng này đang được sử dụng trong Game.");
        }

        if (testQuestionRepo.existsByStemTypeAndStemRefId(StemType.VOCAB, vocabId) ||
                testOptionRepo.existsByContentTypeAndContentRefId(ContentType.VOCAB, vocabId)) {
            throw new BadRequestException("Không thể xóa: Từ vựng này đang được sử dụng trong Test.");
        }

        // 4. XỬ LÝ XÓA (KHI BÀI HỌC CHƯA ACTIVE VÀ CHƯA DÙNG TRONG GAME/TEST)

        // Kiểm tra xem Admin có từng test thử hoặc người dùng cũ (trước khi bài học bị tắt active) đã học chưa
        boolean hasLearned = contentProgressRepo.existsByItemTypeAndItemRefId(ItemType.VOCAB, vocabId);
        boolean hasErrorLog = incorrectItemLogRepo.existsByItemTypeAndItemRefId(ItemType.VOCAB, vocabId);

        if (hasLearned || hasErrorLog) {
            // === XÓA MỀM (Soft Delete) ===
            // Case này hiếm xảy ra nếu quy trình chuẩn, nhưng vẫn giữ để an toàn dữ liệu cũ
            LocalDateTime now = LocalDateTime.now();
            vocab.setDeletedAt(now);

            if (vocab.getMediaAssets() != null) {
                for (MediaAsset media : vocab.getMediaAssets()) {
                    if (media.getDeletedAt() == null) {
                        media.setDeletedAt(now);
                        mediaAssetRepository.save(media);
                    }
                }
            }
            vocabularyRepository.save(vocab);
            return "Soft Deleted: Từ vựng đã được ẩn (do có dữ liệu lịch sử).";
        } else {
            // === XÓA CỨNG (Hard Delete) ===
            // Đây là trường hợp phổ biến nhất khi Admin đang soạn bài
            vocabularyRepository.delete(vocab);
            return "Hard Deleted: Từ vựng đã được xóa vĩnh viễn.";
        }
    }

}
