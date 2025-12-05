package com.iuh.WiseOwlEnglish_Backend.listener;

import com.iuh.WiseOwlEnglish_Backend.event.LessonContentChangedEvent;
import com.iuh.WiseOwlEnglish_Backend.model.LessonProgress;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonProgressRepository;
import com.iuh.WiseOwlEnglish_Backend.service.LessonCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LessonEventListener {
    private final LessonProgressRepository lessonProgressRepo;
    private final LessonCalculatorService calculatorService;
    /**
     * Hàm này sẽ chạy ASYNC (bất đồng bộ) mỗi khi có sự kiện LessonContentChangedEvent.
     * Nó không làm Admin phải chờ đợi.
     */
    @Async
    @EventListener
    @Transactional
    public void handleLessonContentChange(LessonContentChangedEvent event) {
        Long lessonId = event.getLessonId();
        log.info("♻️ [Background Job] Bắt đầu tính toán lại tiến độ cho Lesson ID: {}", lessonId);

        // 1. Lấy danh sách tất cả người dùng đang học bài này
        List<LessonProgress> progressList = lessonProgressRepo.findByLesson_Id(lessonId);

        if (progressList.isEmpty()) {
            log.info("   -> Không có user nào đang học bài này. Bỏ qua.");
            return;
        }

        log.info("   -> Tìm thấy {} users bị ảnh hưởng. Đang xử lý...", progressList.size());

        // 2. Duyệt qua từng user và tính lại %
        int count = 0;
        for (LessonProgress lp : progressList) {
            try {
                Long learnerId = lp.getLearnerProfile().getId();

                // Gọi hàm calculateCurrentProgress từ CalculatorService (bạn đã có hàm này ở câu trả lời trước)
                // Lưu ý: Chúng ta cần hàm 'recalculateLessonPercentage' nhưng version update DB
                // Ở đây ta sẽ gọi hàm recalculateLessonPercentage hiện có của bạn.
                // Vì hàm đó yêu cầu lastItemType/RefId, ta lấy lại từ chính LP hiện tại để không làm mất dấu

                calculatorService.recalculateLessonPercentage(
                        learnerId,
                        lessonId,
                        lp.getLastItemType(),  // Giữ nguyên trạng thái cũ
                        lp.getLastItemRefId()  // Giữ nguyên trạng thái cũ
                );
                count++;
            } catch (Exception e) {
                log.error("❌ Lỗi khi tính lại cho user id={}: {}", lp.getLearnerProfile().getId(), e.getMessage());
            }
        }

        log.info("✅ [Background Job] Hoàn tất. Đã cập nhật tiến độ cho {} users.", count);
    }
}
