package com.iuh.WiseOwlEnglish_Backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.WiseOwlEnglish_Backend.dto.request.AnswerReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.PairDTO;
import com.iuh.WiseOwlEnglish_Backend.dto.request.SubmitTestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.TestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonWithTestsRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.TestsOfLessonRes;
import com.iuh.WiseOwlEnglish_Backend.enums.*;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.iuh.WiseOwlEnglish_Backend.enums.StemType.TEXT;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {
    private final TestRepository testRepository;
    private final TestQuestionRepository testQuestionRepository;
    private final TestOptionRepository testOptionRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;
    private final LearnerProfileRepository learnerRepo;
    private final TestAttemptRepository attemptRepository;
    private final TestAnswerRepository answerRepository;

    private final IncorrectItemLogService incorrectItemLogService;

    private final TransactionTemplate transactionTemplate;

    public TestRes getTestById(Long id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found test: " + id));

        TestRes testRes = new TestRes();
        testRes.setId(test.getId());
        testRes.setLessonId(test.getLessonTest().getId());
        testRes.setTitle(test.getTitle());
        testRes.setType(test.getTestType().toString());
        testRes.setDescription(test.getDescription());
        testRes.setDurationMin(test.getDurationMin());
        testRes.setActive(test.getActive());

        List<TestQuestion> testQuestionList = testQuestionRepository.findByTestIdOrderByOrderInTest(test.getId());
        List<TestQuestionRes> testQuestionResList = new ArrayList<>();

        for (TestQuestion question : testQuestionList) {
            TestQuestionRes questionRes = new TestQuestionRes();
            questionRes.setId(question.getId());
            questionRes.setQuestionType(question.getQuestionType().toString());
            questionRes.setDifficult(question.getDifficulty());
            questionRes.setMaxScore(question.getMaxScore());
            questionRes.setPosition(question.getOrderInTest());

            // 1. Xử lý Stem (Thân câu hỏi: Ảnh/Audio/Text)
            if (question.getStemType() == StemType.IMAGE || question.getStemType() == StemType.AUDIO) {
                if (question.getStemRefId() != null) {
                    MediaAsset mediaAsset = mediaAssetRepository.findById(question.getStemRefId())
                            .orElse(null);
                    if (mediaAsset != null) {
                        questionRes.setMediaUrl(mediaAsset.getUrl());
                    }
                }
            }

            if(question.getStemType()==StemType.SENTENCE){
                Sentence sentence = sentenceRepository.findById(question.getStemRefId())
                        .orElseThrow(()-> new NotFoundException("Khong tim thay sentence co id :"+question.getStemRefId()));
                questionRes.setQuestionContent(sentence.getSentence_en());
            }else if(question.getStemText()!=null){
                questionRes.setQuestionContent(question.getStemText());
            }

            // 2. Xử lý Hidden Word (Quan trọng cho SENTENCE_HIDDEN_WORD)
            questionRes.setHiddenWord(question.getHiddenWord());

            // 3. Xử lý Options
            List<TestOption> testOptionList = testOptionRepository.findByQuestionIdOrderByOrder(question.getId());
            List<TestOptionRes> testOptionResList = new ArrayList<>();

            for (TestOption option : testOptionList) {
                TestOptionRes optionRes = new TestOptionRes();
                optionRes.setId(option.getId());
                optionRes.setCorrect(option.isCorrect());
                optionRes.setPosition(option.getOrder());
                optionRes.setSide(option.getSide() != null ? option.getSide().toString() : null);
                optionRes.setPairKey(option.getPairKey());

                // Xử lý Content của Option dựa trên ContentType
                ContentType ct = option.getContentType();
                if (ct == null) {
                    // Fallback cho các option cũ hoặc dạng Text đơn giản
                    optionRes.setOptionText(option.getText());
                } else {
                    switch (ct) {
                        case VOCAB -> vocabularyRepository.findById(option.getContentRefId())
                                .ifPresent(v -> optionRes.setOptionText(v.getTerm_en()));

                        case SENTENCE -> sentenceRepository.findById(option.getContentRefId())
                                .ifPresent(s -> optionRes.setOptionText(s.getSentence_en()));

                        case IMAGE -> mediaAssetRepository.findById(option.getContentRefId())
                                .ifPresent(m -> optionRes.setImgUrl(m.getUrl()));

                        default -> optionRes.setOptionText(option.getText());
                    }
                }
                testOptionResList.add(optionRes);
            }
            questionRes.setOptions(testOptionResList);
            testQuestionResList.add(questionRes);
        }

        testRes.setQuestionRes(testQuestionResList);
        return testRes;
    }



    public SubmitTestRes submitAndGrade(Long learnerId, Long testId, SubmitTestReq req) {
        try {
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new NotFoundException("Test not found"));

            LearnerProfile learner = learnerRepo.findById(learnerId)
                    .orElseThrow(() -> new NotFoundException("Learner not found"));

            //lay danh sach cau hoi cua bai kiem tra
            //tao qMap de tra cuu questionId->TestQuestion : TestQuestion q = qMap.get(102);
            //tao mot list questionIds chi chua cac questionID
            List<TestQuestion> questions = testQuestionRepository.findByTestIdOrderByOrderInTest(testId);
            Map<Long, TestQuestion> qMap = questions.stream()
                    .collect(Collectors.toMap(TestQuestion::getId, q -> q));
            List<Long> questionIds = questions.stream().map(TestQuestion::getId).toList();

            //gom nhoms các đáp án theo từng câu hỏi . vd : 101 => [optionId 1 ("Dog"), optionId 2 ("Cat")],
            Map<Long, List<TestOption>> optsByQ = testOptionRepository.findByQuestionIdIn(questionIds).stream()
                    .collect(Collectors.groupingBy(o -> o.getQuestion().getId()));

            // Validate all answers belong to this test
            Set<Long> validQIds = new HashSet<>(questionIds);
            for (AnswerReq a : req.getAnswers()) {
                if (!validQIds.contains(a.getQuestionId())) {
                    throw new BadRequestException("Question " + a.getQuestionId() + " not in test");
                }
            }

            // Optional: check time overrun
            Integer limitMin = Optional.ofNullable(test.getDurationMin()).orElse(20);
            int actualSec = calcActualSec(req.getStartedAt(), req.getFinishedAt());
            System.out.println("THOI GIAN THUC LAM BAI :"+actualSec);
            System.out.println("Bat dau :" + req.getStartedAt());
            System.out.println("Ket thuc :" + req.getFinishedAt());
            if (actualSec > limitMin * 60 + 5) { /* cho 5s tolerance */
                // tuỳ chính sách: vẫn chấm nhưng có cờ, hoặc cắt điểm
            }

            TestAttempt attempt = new TestAttempt();
            attempt.setLearnerProfile(learner);
            attempt.setTest(test);
            attempt.setStartedAt(req.getStartedAt());
            attempt.setFinishedAt(req.getFinishedAt());
            attempt.setDurationMin(actualSec / 60);
            attempt.setQuestionCount(questions.size());
            attempt.setStatus(TestAttemptStatus.IN_PROGRESS);
            attempt = attemptRepository.save(attempt);

            int correct = 0, wrong = 0;
            double totalScore = 0.0;
            List<QuestionResultRes> details = new ArrayList<>();

            //cho phép tra nhanh questionId->câu trả lời. vd : 101 => AnswerReq{questionId=101, optionId=555},
            Map<Long, AnswerReq> byQ = req.getAnswers().stream()
                    .collect(Collectors.toMap(AnswerReq::getQuestionId, a -> a, (a,b)->a));

            for (TestQuestion q : questions) {
                try {
                    //lấy câu trả lời theo questionId
                    AnswerReq a = byQ.get(q.getId()); // có thể null → coi như bỏ qua

                    //lấy các option của câu hỏi
                    List<TestOption> opts = optsByQ.getOrDefault(q.getId(), List.of());

                    //lấy điểm của câu hỏi
                    int maxScore = Optional.ofNullable(q.getMaxScore()).orElse(1);

                    //truyền vào câu hỏi, các option và đáp án của người học để kiểm tra kết quả
                    GradeResult gr = gradeOne(q, opts, a); // tính đúng/sai + điểm + đúng là gì

                    // Lưu TestAnswer theo loại câu:
                    try {
                        persistAnswer(attempt, q, a, gr, opts);
                    } catch (Exception e) {
                        log.error("persistAnswer failed for questionId={} attemptId={}", q.getId(), attempt.getId(), e);
                        // tùy chính sách: continue (bỏ qua câu này) hoặc rethrow. Mình continue để không làm hỏng toàn bài.
                    }

                    if (gr == null) {
                        log.warn("gradeOne returned null for questionId={}", q.getId());
                        // tạo GradeResult mặc định
                        gr = new GradeResult(false, 0.0, List.of());
                    }

                    if (gr.correct()) {
                        correct++;
                        totalScore += gr.earnedScore();
                    } else {
                        wrong++;
                    }

                    // GỌI LOGIC MỚI (luôn luôn gọi) — bọc try/catch để tránh exception lan ra
                    try {
                        Long lessonId = (test.getLessonTest() != null) ? test.getLessonTest().getId() : null;
                        incorrectItemLogService.logTestOptions(
                                learner.getId(),
                                lessonId,
                                q,
                                opts,
                                gr.correct() // Truyền kết quả
                        );
                    } catch (Exception e) {
                        log.error("incorrectItemLogService.logTestOptions failed for qId={}", q.getId(), e);
                    }

                    QuestionResultRes resultRes = new QuestionResultRes();
                    resultRes.setQuestionId(q.getId());
                    resultRes.setQuestionType(q.getQuestionType() == null ? null : q.getQuestionType().toString());
                    resultRes.setCorrect(gr.correct);
                    resultRes.setEarnedScore(gr.earnedScore);
                    resultRes.setMaxScore(maxScore);
                    if (a != null) {
                        resultRes.setSelectedOptionId(a.getOptionId());
                        resultRes.setSelectedOptionIds(a.getOptionIds()); // nếu có dùng MULTI_SELECT
                    } else {
                        resultRes.setSelectedOptionId(null);
                        resultRes.setSelectedOptionIds(null);
                    }

                    resultRes.setCorrectOptionIds(gr.correctOptionIds() == null ? List.of() : gr.correctOptionIds());
                    details.add(resultRes);

                } catch (Exception e) {
                    // Bắt mọi ngoại lệ bất ngờ ở mức câu hỏi để xem stacktrace + tiếp tục chấm câu còn lại
                    log.error("Failed while grading question id={} in submitAndGrade", q.getId(), e);
                }
            }

            attempt.setCorrectCount(correct);
            attempt.setWrongCount(wrong);
            attempt.setScore(totalScore);
            attempt.setStatus(TestAttemptStatus.FINISHED);
            attemptRepository.save(attempt);

            SubmitTestRes res = new SubmitTestRes();
            res.setAttemptId(attempt.getId());
            res.setTestId(test.getId());
            res.setScore(attempt.getScore());
            res.setCorrectCount(attempt.getCorrectCount());
            res.setWrongCount(attempt.getWrongCount());
            res.setQuestionCount(attempt.getQuestionCount());
            res.setDurationSec(actualSec);
            res.setQuestionResults(details); // chính là List<QuestionResultRes>

            return res;


        }catch (Exception e) {
            // log full stacktrace (rất quan trọng để biết line ném NPE)
            log.error("submitAndGrade failed", e);
            throw e; // rethrow để behavior không bị silent (hoặc return error DTO tuỳ policy)
        }

    }

    private int calcActualSec(LocalDateTime s, LocalDateTime f) {
        if (s == null || f == null) return 0;
        return (int) Duration.between(s, f).getSeconds();
    }

    // Kết quả chấm cho 1 câu
    private record GradeResult(
            boolean correct,
            double earnedScore,
            List<Long> correctOptionIds) {}

    private GradeResult gradeOne(TestQuestion q, List<TestOption> opts, AnswerReq a) {
        int maxScore = Optional.ofNullable(q.getMaxScore()).orElse(1);

        //lấy ra những đáp án đúng rồi gom lại thành 1 list chứa id của các đáp án đúng
        List<Long> correctIds = opts.stream()
                .filter(TestOption::isCorrect)
                .map(TestOption::getId).toList();

        switch (q.getQuestionType()) {
            case PICTURE_WORD_MATCHING,SOUND_WORD_MATCHING,PICTURE_SENTENCE_MATCHING -> {
                boolean ok = (a != null && a.getOptionId() != null && correctIds.contains(a.getOptionId()));
                return new GradeResult(ok, ok ? maxScore : 0.0, correctIds);
            }
            case PICTURE4_WORD4_MATCHING -> {
                boolean ok = checkMatchingExactly(a, opts);
                return new GradeResult(ok, ok ? maxScore : 0.0, correctIds);
            }
            case WORD_TO_SENTENCE -> {
                boolean ok = checkOrderingExactly(a, opts);
                return new GradeResult(ok, ok ? maxScore : 0.0, correctIds);
            }
            case SENTENCE_HIDDEN_WORD -> {
                String gold = correctTextFromOptions(opts);
                if (gold.isBlank()) {
                    log.warn("No gold text found for questionId={}, optsCount={}", q.getId(), opts == null ? 0 : opts.size());
                }
                boolean ok = (a != null && a.getTextInput() != null && normalize(a.getTextInput()).equals(gold));
                return new GradeResult(ok, ok ? maxScore : 0.0, correctIds);
            }
            case PICTURE_WORD_WRITING ->{
                TestOption option = opts.stream()
                        .filter(TestOption::isCorrect)
                        .findFirst()
                        .orElse(null);

                if (option == null) {
                    log.warn("No correct option found for options size={}", opts.size());
                }
                Vocabulary vocabulary = vocabularyRepository.findById(option.getContentRefId())
                        .orElseThrow(()-> new NotFoundException("Khong tim thay vocab co id:"+option.getContentRefId()));
                String vocabText = normalize(vocabulary.getTerm_en());
                boolean ok = vocabText.equals(normalize(a.getTextInput()));
                return new GradeResult(ok, ok ? maxScore : 0.0, correctIds);


            }
            default -> {
                return new GradeResult(false, 0.0, correctIds);
            }
        }
    }

    private void persistAnswer(TestAttempt attempt, TestQuestion q, AnswerReq a, GradeResult gr, List<TestOption> opts) {
        TestAnswer ta = new TestAnswer();
        ta.setAttempt(attempt);
        ta.setQuestion(q);
        ta.setCreatedAt(LocalDateTime.now());
        ta.setCorrect(gr.correct());

        if (a != null) {
            if (a.getOptionId() != null) {
                TestOption op = opts.stream()
                        .filter(o -> o.getId().equals(a.getOptionId()))
                        .findFirst().orElse(null);
                ta.setOption(op);
            }
            if (a.getSequence() != null) {
                ta.setSequenceJson(writeJson(a.getSequence()));
            }
            if (a.getPairs() != null) {
                ta.setPairsJson(writeJson(a.getPairs()));
            }
            if (a.getTextInput()!= null) ta.setTextInput(a.getTextInput());
            if (a.getNumericInput() != null) ta.setNumericInput(a.getNumericInput());
        }

        answerRepository.save(ta);
    }

    // helpers (tuỳ bạn implement thực)
    private boolean checkOrderingExactly(AnswerReq a, List<TestOption> opts) {
        if (a == null || a.getSequence() == null) return false;
        // so sánh optionId theo correctOrder
        List<Long> correctSeq = opts.stream()
                .sorted(Comparator.comparing(TestOption::getCorrectOrder))
                .map(TestOption::getId).toList();
        return correctSeq.equals(a.getSequence());
    }

    private boolean checkMatchingExactly(AnswerReq a, List<TestOption> opts) {
        if (a == null || a.getPairs() == null) return false;
        // bạn cần có cách xác định pair đúng (vd dựa pairKey)
        Map<Long, String> keyById = opts.stream()
                .collect(Collectors.toMap(TestOption::getId, TestOption::getPairKey));
        for (PairDTO p : a.getPairs()) {
            String l = keyById.get(p.getLeftOptionId());
            String r = keyById.get(p.getRightOptionId());
            if (l == null || r == null || !l.equals(r)) return false;
        }
        return true;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase();
    }

    private String correctTextFromOptions(List<TestOption> opts) {
        // Trường hợp null/empty guard
        if (opts == null || opts.isEmpty()) {
            log.warn("correctTextFromOptions: options list is null or empty");
            return "";
        }

        // Giả sử TestOption có method getText() trả nội dung text (tùy class của bạn)
        // Lọc option có isCorrect == true, lấy text đầu tiên nếu có, trim và trả về.
        return opts.stream()
                .filter(Objects::nonNull)
                .filter(TestOption::isCorrect)
                .map(opt -> {
                    // Nếu text field có tên khác, thay getText() tương ứng
                    String txt = null;
                    try {
                        txt = opt.getText(); // sửa nếu tên field khác, ví dụ getContent()
                    } catch (Exception e) {
                        log.warn("correctTextFromOptions: failed to get text from option id={}", opt == null ? null : opt.getId(), e);
                    }
                    return txt;
                })
                .filter(Objects::nonNull)
                .map(this::normalize) // nếu bạn muốn normalize ở đây luôn
                .findFirst()
                .orElse("");
    }

    private String writeJson(Object o) {
        try { return new ObjectMapper().writeValueAsString(o); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    // lay danh sach test theo lessonId

    public List<TestResByLesson> getTestsByLessonId(Long lessonId){
        if(lessonId==null){
            throw new BadRequestException("LessonId đang là null");
        }


        List<Test> testList = testRepository.findByLessonTest_Id(lessonId);
        if (testList == null || testList.isEmpty()) {
            return new ArrayList<>();
        }

        List<TestResByLesson> testResList = new ArrayList<>();
        for(Test test:testList){
            if (Boolean.TRUE.equals(test.getActive()) && test.getDeletedAt() == null) {
                TestResByLesson testRes = new TestResByLesson();
                testRes.setId(test.getId());
                testRes.setLessonId(test.getLessonTest().getId());
                testRes.setTitle(test.getTitle());
                testRes.setType(test.getTestType().toString());
                testRes.setDescription(test.getDescription());
                testRes.setDurationMin(test.getDurationMin());
                testRes.setActive(test.getActive());
                testResList.add(testRes);
            }

        }
        return testResList;
    }
}



