package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.TestOptionReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.TestQuestionReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.TestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestResByLesson;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonWithTestsRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.TestAdminByLessonRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.TestsOfLessonRes;
import com.iuh.WiseOwlEnglish_Backend.enums.*;
import com.iuh.WiseOwlEnglish_Backend.event.LessonContentChangedEvent;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestAdminService {
    private final TestRepository testRepository;
    private final TestAttemptRepository attemptRepository;
    private final LessonRepository lessonRepository;
    private final TransactionTemplate transactionTemplate;
    private final TestQuestionRepository testQuestionRepository;
    private final SentenceRepository sentenceRepository; // 2. Inject thêm Repository này

    // 1. Inject Publisher
    private final ApplicationEventPublisher eventPublisher;

    public TestsOfLessonRes getTestsByLessonId(Long lessonId) {
        if (lessonId == null) {
            throw new BadRequestException("LessonId đang là null");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(()-> new NotFoundException("Khong tim thay lesson id :"+lessonId));
        TestsOfLessonRes res1 = new TestsOfLessonRes();
        res1.setId(lesson.getId());
        res1.setUnitNumber(lesson.getUnitName());
        res1.setUnitName(lesson.getLessonName());

        // 1. Lấy danh sách Test theo LessonId
        // (Lưu ý: Nên đảm bảo method này trong Repo đã lọc deletedAt IS NULL như các bước trước)
        List<Test> testList = testRepository.findByLessonTest_Id(lessonId);


        List<TestAdminByLessonRes> testResList = new ArrayList<>();
        for (Test test : testList) {
            // Bỏ qua bài test đã bị xoá mềm (nếu Repo chưa lọc)
            if (test.getDeletedAt() != null) continue;

            TestAdminByLessonRes res = new TestAdminByLessonRes();
            res.setId(test.getId());
            res.setLessonId(test.getLessonTest().getId());
            res.setTitle(test.getTitle());
            res.setType(test.getTestType().toString());
            res.setDescription(test.getDescription());
            res.setDurationMin(test.getDurationMin());
            res.setActive(test.getActive());

            // 👇 TÍNH TOÁN CÁC TRƯỜNG MỚI

            // 2. Tính tổng số câu hỏi
            // (Vì fetch Lazy nên khi gọi size() hibernate sẽ query list questions nếu chưa load)
            int totalQ = (test.getQuestions() != null) ? test.getQuestions().size() : 0;
            res.setTotalQuestion(totalQ);

            // 3. Kiểm tra đã có người học làm bài chưa
            boolean hasUserAttempt = attemptRepository.existsByTest_Id(test.getId());
            res.setHasAttempt(hasUserAttempt);

            testResList.add(res);
        }
        res1.setTestList(testResList);
        return res1;
    }

    public List<LessonWithTestsRes> getTestsByGradeId(Long gradeId) {
        // 1. Lấy danh sách tất cả Lesson thuộc Grade (chưa bị xoá), sắp xếp theo thứ tự
        List<Lesson> lessons = lessonRepository.findByGradeLevel_IdAndDeletedAtIsNullOrderByOrderIndexAsc(gradeId);

        if (lessons.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Lấy tất cả Test thuộc Grade đó (để tránh query N+1 trong vòng lặp)
        // (Giả sử bạn đã có hàm này trong TestRepository như thảo luận trước)
        List<Test> tests = testRepository.findTestsByGradeId(gradeId);

        // 3. Gom nhóm Test theo LessonId để tra cứu nhanh (Map<LessonId, List<Test>>)
        Map<Long, List<Test>> testsByLessonMap = tests.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getLessonTest().getId()
                ));

        // 4. Duyệt qua danh sách Lesson để tạo response
        List<LessonWithTestsRes> result = new ArrayList<>();

        for (Lesson lesson : lessons) {
            LessonWithTestsRes lessonRes = new LessonWithTestsRes();

            // Set thông tin bài học
            lessonRes.setLessonId(lesson.getId());
            lessonRes.setUnitName(lesson.getUnitName());
            lessonRes.setLessonName(lesson.getLessonName());
            lessonRes.setOrderIndex(lesson.getOrderIndex());

            // Lấy danh sách test của lesson này từ Map
            // Nếu không có test nào, getOrDefault sẽ trả về list rỗng []
            List<Test> lessonTests = testsByLessonMap.getOrDefault(lesson.getId(), Collections.emptyList());

            // Map sang DTO TestResByLesson
            List<TestResByLesson> testDtos = lessonTests.stream().map(t -> {
                TestResByLesson dto = new TestResByLesson();
                dto.setId(t.getId());
                dto.setLessonId(lesson.getId());
                dto.setTitle(t.getTitle());
                dto.setType(t.getTestType().toString());
                dto.setDescription(t.getDescription());
                dto.setDurationMin(t.getDurationMin());
                dto.setActive(t.getActive());
                return dto;
            }).toList();

            lessonRes.setTests(testDtos);
            result.add(lessonRes);
        }

        return result;
    }

    private static final int MAX_RETRY = 3;
    private static final long RETRY_SLEEP_MS = 50L;


    // ================== CẬP NHẬT LOGIC TẠO TEST ==================
    @CacheEvict(value = "lessonTotals", key = "#request.lessonId + '_testquestion'")
    public TestRes createTest(TestReq request) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return transactionTemplate.execute(status -> {
                    // 1. Tạo Test Header
                    Test test = new Test();
                    Lesson lesson = lessonRepository.findById(request.getLessonId())
                            .orElseThrow(() -> new RuntimeException("Lesson not found"));
                    test.setLessonTest(lesson);
                    test.setActive(request.getActive());
                    test.setTitle(request.getTitle());
                    test.setTestType(TestType.valueOf(request.getType()));
                    test.setDescription(request.getDescription());
                    test.setDurationMin(request.getDurationMin());
                    test.setCreatedAt(LocalDateTime.now());
                    test.setUpdatedAt(LocalDateTime.now());

                    Test savedTest = testRepository.save(test);

                    // 2. Tính toán order index bắt đầu
                    int maxQuestionOrder = testQuestionRepository.findMaxOrderInTestByTestId(savedTest.getId());
                    int nextQuestionOrder = maxQuestionOrder + 1;

                    // 3. Duyệt qua danh sách câu hỏi
                    for (var qReq : request.getQuestions()) {
                        TestQuestion question = new TestQuestion();
                        question.setTest(savedTest);
                        question.setOrderInTest(nextQuestionOrder++);

                        TestQuestionType type = TestQuestionType.valueOf(qReq.getQuestionType());
                        question.setQuestionType(type);

                        // Set các field chung: IMAGE, AUDIO, SENTENCE
                        question.setStemType(StemType.valueOf(qReq.getStemType()));
                        question.setStemRefId(qReq.getStemRefId());
                        //danh cho dang SENTENCE_HIDDEN_WORD
                        question.setStemText(qReq.getStemText()); // Có thể bị override bên dưới tuỳ loại
                        question.setHiddenWord(qReq.getHiddenWord());

                        question.setDifficulty(1);
                        question.setMaxScore(qReq.getMaxScore());
                        question.setCreatedAt(LocalDateTime.now());
                        question.setUpdatedAt(LocalDateTime.now());

                        // 4. Xử lý Options dựa trên Loại câu hỏi
                        List<TestOption> opts = new ArrayList<>();

                        switch (type) {
                            case SENTENCE_HIDDEN_WORD ->
                                    handleSentenceHiddenWord(question, qReq, opts);

                            case WORD_TO_SENTENCE ->
                                    handleWordToSentence(question, qReq, opts);

                            default ->
                                // Nhóm 5 loại cơ bản: Lấy options từ request
                                    handleStandardOptions(question, qReq.getOptions(), opts);
                        }

                        question.setOptions(opts);
                        testQuestionRepository.save(question);
                    }
                    eventPublisher.publishEvent(new LessonContentChangedEvent(this, request.getLessonId()));

                    // 5. Build Response
                    TestRes res = new TestRes();
                    res.setId(savedTest.getId());
                    res.setLessonId(savedTest.getLessonTest().getId());
                    res.setActive(savedTest.getActive());
                    res.setTitle(savedTest.getTitle());
                    res.setType(savedTest.getTestType().toString());
                    res.setDescription(savedTest.getDescription());
                    res.setDurationMin(savedTest.getDurationMin());

                    return res;
                });
            } catch (DataIntegrityViolationException dive) {
                if (attempt >= MAX_RETRY) {
                    throw new BadRequestException("Không tạo được test (lỗi conflict orderIndex) sau " + MAX_RETRY + " lần thử.");
                }
                try { Thread.sleep(RETRY_SLEEP_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new BadRequestException("Không tạo được test: " + exception.getMessage());
            }
        }
    }

    // --- CÁC HÀM XỬ LÝ OPTION ---

    // 1. Xử lý nhóm câu hỏi thường (Lấy từ DB do Admin chọn)
    private void handleStandardOptions(TestQuestion question, List<TestOptionReq> optionReqs, List<TestOption> opts) {
        if (optionReqs == null) return;

        int optionOrder = 1;
        for (var oReq : optionReqs) {
            TestOption option = new TestOption();
            option.setQuestion(question);

            // Map dữ liệu từ request
            if (oReq.getContentType() != null) {
                option.setContentType(ContentType.valueOf(oReq.getContentType()));
            }
            option.setContentRefId(oReq.getContentRefId());
//            option.setText(oReq.getText());
            option.setCorrect(oReq.isCorrect());
            option.setOrder(optionOrder++);

            if (oReq.getSide() != null) {
                option.setSide(Side.valueOf(oReq.getSide()));
            }
            option.setPairKey(oReq.getPairKey());

            option.setCreatedAt(LocalDateTime.now());
            option.setUpdatedAt(LocalDateTime.now());
            opts.add(option);
        }
    }

    // 2. Xử lý SENTENCE_HIDDEN_WORD (Hệ thống tự mask câu)
    private void handleSentenceHiddenWord(TestQuestion question, TestQuestionReq qReq, List<TestOption> opts) {
        String full = qReq.getStemText();   // Câu đầy đủ
        String hidden = qReq.getHiddenWord(); // Từ cần ẩn

        if (!containsLoose(full, hidden)) {
            throw new BadRequestException("Từ ẩn '" + hidden + "' không có trong câu: " + full);
        }

        // Tạo câu bị ẩn (VD: "I ___ apples")
        String masked = maskFirstOccurrence(full, hidden, "___");
        question.setStemText(masked); // Lưu câu đã đục lỗ vào DB

        // Tạo Option đúng (chứa từ bị ẩn)
        TestOption option = new TestOption();
        option.setQuestion(question);
        option.setText(hidden); // Đáp án là từ bị ẩn
        option.setCorrect(true);
        option.setOrder(1);
//        option.setContentType(ContentType.VOCAB); // Hoặc TEXT tuỳ logic FE
        option.setCreatedAt(LocalDateTime.now());
        option.setUpdatedAt(LocalDateTime.now());

        opts.add(option);
    }

    // 3. Xử lý WORD_TO_SENTENCE (Hệ thống tự tách từ)
    private void handleWordToSentence(TestQuestion question, com.iuh.WiseOwlEnglish_Backend.dto.request.TestQuestionReq qReq, List<TestOption> opts) {
        Sentence sentence = sentenceRepository.findById(question.getStemRefId())
                .orElseThrow(()-> new NotFoundException("Khong tim thay sentence co id trong luc tao option cho WTS:"+question.getStemRefId()));
        // Tách câu thành các token (giữ dấu câu)
        List<String> tokens = tokenizeKeepPunct(sentence.getSentence_en());

        int pos = 1;
        for (String tk : tokens) {
            TestOption opt = new TestOption();
            opt.setQuestion(question);
            opt.setText(tk);
            opt.setOrder(pos);       // Thứ tự xuất hiện
            opt.setCorrectOrder(pos);// Thứ tự đúng (để chấm điểm)
            opt.setCorrect(true);    // Trong bài xếp từ, tất cả thẻ đều là một phần của đáp án
//            opt.setContentType(ContentType.TEXT);
            opt.setCreatedAt(LocalDateTime.now());
            opt.setUpdatedAt(LocalDateTime.now());

            opts.add(opt);
            pos++;
        }
    }

    // --- CÁC HÀM UTILS (Helper) ---

    // Tách câu thành từ, giữ lại dấu câu
    private List<String> tokenizeKeepPunct(String sentence) {
        if (sentence == null || sentence.isBlank()) return List.of();
        // Chèn khoảng trắng quanh dấu câu để split
        String spaced = sentence
                .replaceAll("([.,!?;:])", " $1 ")
                .replaceAll("([()\"“”‘’])", " $1 ")
                .replaceAll("\\s+", " ")
                .trim();
        String[] parts = spaced.split(" ");
        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            if (!p.isBlank()) tokens.add(p);
        }
        return tokens;
    }

    // Che từ đầu tiên tìm thấy
    private String maskFirstOccurrence(String sentence, String word, String placeholder) {
        String regex = "(?i)" + Pattern.quote(word.trim());
        return sentence.replaceFirst(regex, placeholder);
    }

    // Kiểm tra tồn tại (không phân biệt hoa thường/dấu)
    private boolean containsLoose(String sentence, String word) {
        String a = normalize(sentence);
        String b = normalize(word);
        return a.contains(b);
    }

    // Chuẩn hóa chuỗi (lowercase + bỏ dấu)
    private String normalize(String s) {
        if (s == null) return "";
        String t = s.toLowerCase(Locale.ROOT).trim();
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    @Transactional
    public void updateStatus(Long testId, boolean isActive) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài kiểm tra với id: " + testId));

        test.setActive(isActive);
        test.setUpdatedAt(LocalDateTime.now());

        testRepository.save(test);
    }
    public List<String> getQuestionTypesByLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học với id: " + lessonId));

        int gradeOrder = lesson.getGradeLevel().getOrderIndex();

        // Danh sách chung cho lớp 1, 2
        List<String> typesForGrade1And2 = List.of(
                TestQuestionType.PICTURE_WORD_MATCHING.name(),
                TestQuestionType.PICTURE4_WORD4_MATCHING.name(), // Đã thêm theo yêu cầu
                TestQuestionType.SOUND_WORD_MATCHING.name(),
                TestQuestionType.PICTURE_SENTENCE_MATCHING.name()
        );

        if (gradeOrder <= 2) {
            return typesForGrade1And2;
        } else {
            // Lớp 3, 4, 5: Bao gồm 4 loại trên + 3 loại nâng cao
            List<String> typesForUpperGrades = new ArrayList<>(typesForGrade1And2);
            typesForUpperGrades.add(TestQuestionType.PICTURE_WORD_WRITING.name());
            typesForUpperGrades.add(TestQuestionType.SENTENCE_HIDDEN_WORD.name());
            typesForUpperGrades.add(TestQuestionType.WORD_TO_SENTENCE.name());

            return typesForUpperGrades;
        }
    }
    @Transactional
    public String deleteTest(Long testId) {
        // 1. Tìm Test
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new NotFoundException("Test not found with id: " + testId));

        // 2. Kiểm tra điều kiện
        // Lesson chứa test có đang active không?
        boolean isLessonActive = test.getLessonTest().isActive();
        // Test đã có ai làm chưa?
        boolean hasAttempts = attemptRepository.existsByTest_Id(testId);
        if(isLessonActive){
            throw new BadRequestException("Bài kiểm tra nằm trong bài học đang được kích hoạt nên không được xoá. Vui lòng tắt kích hoạt bài học!\n");
        }
        // 3. Phân nhánh xử lý
        else if (!hasAttempts) {
            // === TRƯỜNG HỢP 1: XOÁ CỨNG (HARD DELETE) ===
            // Lesson chưa active VÀ chưa ai làm bài -> Dữ liệu rác -> Xoá sạch

            // Do Test có cascade = CascadeType.ALL với questions
            // và TestQuestion có cascade = CascadeType.ALL với options
            // -> Xoá Test sẽ tự động xoá hết câu hỏi và đáp án liên quan.
            testRepository.delete(test);
            eventPublisher.publishEvent(new LessonContentChangedEvent(this, test.getLessonTest().getId()));
            return "Đã xoá vĩnh viễn Test (Hard Delete) vì chưa có dữ liệu người dùng.";
        } else {
            // === TRƯỜNG HỢP 2: XOÁ MỀM (SOFT DELETE) ===
            // Cập nhật deletedAt cho Test, Question và Option

            LocalDateTime now = LocalDateTime.now();

            // A. Xoá mềm Test
            test.setDeletedAt(now);
            test.setActive(false);

            // B. Xoá mềm Questions và Options (Cascading Soft Delete)
            // Kiểm tra null safety cho list questions
            if (test.getQuestions() != null) {
                for (TestQuestion question : test.getQuestions()) {
                    // Xoá mềm Question
                    question.setDeletedAt(now);

                    // Xoá mềm Options của Question đó
                    if (question.getOptions() != null) {
                        for (TestOption option : question.getOptions()) {
                            option.setDeletedAt(now);
                        }
                    }
                }
            }

            // C. Lưu thay đổi
            // Nhờ CascadeType.ALL (hoặc MERGE) trong Entity Test -> Question -> Option
            // JPA sẽ tự động update SQL cho tất cả các entity con đã bị thay đổi field.
            testRepository.save(test);

            eventPublisher.publishEvent(new LessonContentChangedEvent(this, test.getLessonTest().getId()));
            return "Đã xoá mềm Test và toàn bộ câu hỏi/đáp án liên quan để bảo toàn lịch sử.";
        }
    }
}
