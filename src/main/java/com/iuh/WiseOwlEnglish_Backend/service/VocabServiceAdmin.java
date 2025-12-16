package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.VocabUpdateReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.CreateVocabReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.VocabRes;
import com.iuh.WiseOwlEnglish_Backend.enums.*;
import com.iuh.WiseOwlEnglish_Backend.event.LessonContentChangedEvent;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
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

    // Inject Publisher
    private final ApplicationEventPublisher eventPublisher;


    public List<VocabRes> getListVocab(long lessonId){
        List<Vocabulary> vocabularyList = vocabularyRepository. findByLessonVocabularyIdAndDeletedAtIsNullOrderByOrderIndexAsc(lessonId);
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
        vocabRes.setTerm_vi(vocabulary.getTerm_vi());
        vocabRes.setPartOfSpeech(vocabulary.getPartOfSpeech());
        vocabRes.setPhonetic(vocabulary.getPhonetic());
        vocabRes.setForLearning(vocabulary.isForLearning());
        // 1. Xử lý Image
        MediaAsset mediaImg = mediaAssetRepository.findByVocabularyIdAndMediaType(vocabulary.getId(), MediaType.IMAGE);
        // Nếu mediaImg khác null thì lấy URL, ngược lại thì gán null
        vocabRes.setImgUrl(mediaImg != null ? mediaImg.getUrl() : null);

        // 2. Xử lý Audio Normal
        MediaAsset audioNormal = mediaAssetRepository.findByVocabularyIdAndMediaTypeAndTag(vocabulary.getId(), MediaType.AUDIO, "normal");
        vocabRes.setAudioNormal(audioNormal != null ? audioNormal.getUrl() : null);

        // 3. Xử lý Audio Slow
        MediaAsset audioSlow = mediaAssetRepository.findByVocabularyIdAndMediaTypeAndTag(vocabulary.getId(), MediaType.AUDIO, "slow");
        vocabRes.setAudioSlow(audioSlow != null ? audioSlow.getUrl() : null);
        return vocabRes;
    }
    // Khi tạo Vocab -> Xóa cache đếm Vocab của lesson đó
    @CacheEvict(value = "lessonTotals", key = "#req.lessonId + '_vocab'")
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
                // 👇 KÍCH HOẠT SỰ KIỆN CHẠY NGẦM 👇
                if (result != null) {
                    eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
                }

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

    /**
     * Hàm Import Excel
     * @param file File excel tải lên
     * @param lessonId ID của bài học muốn import vào
     * @return Danh sách các từ vựng đã import thành công hoặc danh sách lỗi
     */
    public List<String> importVocabulariesFromExcel(MultipartFile file, Long lessonId) {
        List<String> errorLogs = new ArrayList<>();
        List<CreateVocabReq> vocabReqs = new ArrayList<>();

        // 1. Đọc file Excel và map sang DTO
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên
            DataFormatter dataFormatter = new DataFormatter(); // Helper để đọc cell thành String an toàn

            // Duyệt từ dòng thứ 1 (bỏ qua dòng tiêu đề index 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Kiểm tra nếu ô từ vựng chính bị rỗng thì bỏ qua dòng này
                String termEn = dataFormatter.formatCellValue(row.getCell(0));
                if (termEn == null || termEn.trim().isEmpty()) continue;

                try {
                    CreateVocabReq req = new CreateVocabReq();

                    // Gán cứng lessonId từ tham số truyền vào
                    req.setLessonId(lessonId);

                    // Map dữ liệu từng cột (Lưu ý thứ tự cột phải khớp file mẫu)
                    req.setTerm_en(termEn);
                    req.setTerm_vn(dataFormatter.formatCellValue(row.getCell(1)));
                    req.setPhonetic(dataFormatter.formatCellValue(row.getCell(2)));
                    req.setPartOfSpeech(dataFormatter.formatCellValue(row.getCell(3)));

                    // Xử lý boolean
                    String isLearningStr = dataFormatter.formatCellValue(row.getCell(4));
                    req.setForLearning(Boolean.parseBoolean(isLearningStr) || "1".equals(isLearningStr));

                    // URL Media
                    req.setUrlImg(dataFormatter.formatCellValue(row.getCell(5)));
                    req.setUrlAudioNormal(dataFormatter.formatCellValue(row.getCell(6)));
                    req.setUrlAudioSlow(dataFormatter.formatCellValue(row.getCell(7)));

                    // Xử lý duration (số nguyên)
                    String durNormStr = dataFormatter.formatCellValue(row.getCell(8));
                    req.setDurationSecNormal(durNormStr.isEmpty() ? 0 : (int) Double.parseDouble(durNormStr));

                    String durSlowStr = dataFormatter.formatCellValue(row.getCell(9));
                    req.setDurationSecSlow(durSlowStr.isEmpty() ? 0 : (int) Double.parseDouble(durSlowStr));

                    vocabReqs.add(req);

                } catch (Exception e) {
                    errorLogs.add("Dòng " + (i + 1) + ": Lỗi format dữ liệu - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }

        // 2. Thực hiện tạo từ vựng (Tận dụng hàm createVocab có sẵn)
        int successCount = 0;
        for (CreateVocabReq req : vocabReqs) {
            try {
                // Gọi lại hàm createVocab bạn đã viết
                // Hàm này đã bao gồm logic: Retry orderIndex, Save Media, Publish Event, Evict Cache
                this.createVocab(req);
                successCount++;
            } catch (Exception e) {
                errorLogs.add("Lỗi import từ '" + req.getTerm_en() + "': " + e.getMessage());
            }
        }

        errorLogs.add(0, "Đã import thành công: " + successCount + "/" + vocabReqs.size() + " từ vựng.");
        return errorLogs;
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
        Long lessonId = vocab.getLessonVocabulary().getId();

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
            // 👇 KÍCH HOẠT SỰ KIỆN CHẠY NGẦM (Ở cuối hàm, trước khi return) 👇
            eventPublisher.publishEvent(new LessonContentChangedEvent(this, lessonId));
            return "Soft Deleted: Từ vựng đã được ẩn (do có dữ liệu lịch sử).";
        } else {
            // === XÓA CỨNG (Hard Delete) ===
            // Đây là trường hợp phổ biến nhất khi Admin đang soạn bài
            vocabularyRepository.delete(vocab);
            eventPublisher.publishEvent(new LessonContentChangedEvent(this, lessonId));
            return "Hard Deleted: Từ vựng đã được xóa vĩnh viễn.";
        }
    }

    public VocabRes updateVocabulary(Long id, VocabUpdateReq req) {
        // 1. Tìm từ vựng, nếu không thấy thì báo lỗi
        Vocabulary vocab = vocabularyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy từ vựng với ID: " + id));

        // 2. Cập nhật các trường dữ liệu từ Request
        vocab.setTerm_en(req.getTerm_en());
        vocab.setTerm_vi(req.getTerm_vi());
        vocab.setPhonetic(req.getPhonetic());
        vocab.setPartOfSpeech(req.getPartOfSpeech());
        vocab.setForLearning(req.isForLearning());

        MediaAsset mediaImg = mediaAssetRepository.findByVocabularyIdAndMediaType(vocab.getId(),MediaType.IMAGE);
        mediaImg.setUrl(req.getImgUrl());

        MediaAsset mediaAudioNormal = mediaAssetRepository.findByVocabularyIdAndMediaTypeAndTag(vocab.getId(),MediaType.AUDIO,"normal");
        mediaAudioNormal.setUrl(req.getAudioNormal());


        MediaAsset mediaAudioSlow = mediaAssetRepository.findByVocabularyIdAndMediaTypeAndTag(vocab.getId(),MediaType.AUDIO,"slow");
        mediaAudioSlow.setUrl(req.getAudioSlow());

        // 3. Hệ thống tự cập nhật thời gian
        vocab.setUpdatedAt(LocalDateTime.now());

        // 4. Lưu xuống database
        Vocabulary savedVocab = vocabularyRepository.save(vocab);

        // 5. Trả về DTO
        VocabRes res = new VocabRes();
        res.setId(savedVocab.getId());
        res.setOrderIndex(savedVocab.getOrderIndex());
        res.setTerm_en(savedVocab.getTerm_en());
        res.setTerm_vi(savedVocab.getTerm_vi());
        res.setPhonetic(savedVocab.getPhonetic());
        res.setPartOfSpeech(savedVocab.getPartOfSpeech());
        MediaAsset mediaImgRes = mediaAssetRepository.findByVocabularyIdAndMediaType(savedVocab.getId(),MediaType.IMAGE);
        res.setImgUrl(mediaImgRes.getUrl());

        MediaAsset mediaAudioNormalRes = mediaAssetRepository.findByVocabularyIdAndMediaTypeAndTag(savedVocab.getId(),MediaType.AUDIO,"normal");
        res.setAudioNormal(mediaAudioNormalRes.getUrl());

        MediaAsset mediaAudioSlowRes = mediaAssetRepository.findByVocabularyIdAndMediaTypeAndTag(savedVocab.getId(),MediaType.AUDIO,"slow");
        res.setAudioSlow(mediaAudioNormalRes.getUrl());

        res.setForLearning(savedVocab.isForLearning());

        return res;
    }

}
