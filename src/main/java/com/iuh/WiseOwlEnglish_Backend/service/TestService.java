package com.iuh.WiseOwlEnglish_Backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.WiseOwlEnglish_Backend.dto.request.AnswerReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.PairDTO;
import com.iuh.WiseOwlEnglish_Backend.dto.request.SubmitTestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.TestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonWithTestsRes;
import com.iuh.WiseOwlEnglish_Backend.enums.*;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

    private static final int MAX_RETRY = 3;
    private static final long RETRY_SLEEP_MS = 50L;

    //ADMIN FUNCTIONALITY
    public TestRes createTest(TestReq request) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                // each attempt runs inside its own transaction
                return transactionTemplate.execute(status -> {
                    // --- create Test ---
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

                    Test savedTest = testRepository.save(test); // persisted and has id

                    // --- determine starting order for questions (max existing order) ---
                    int maxQuestionOrder = testQuestionRepository.findMaxOrderInTestByTestId(savedTest.getId());
                    int nextQuestionOrder = maxQuestionOrder + 1;

                    for (var qReq : request.getQuestions()) {
                        TestQuestion question = new TestQuestion();
                        question.setTest(savedTest);

                        // System assigns orderInTest (no input from user)
                        question.setOrderInTest(nextQuestionOrder++);
                        question.setQuestionType(TestQuestionType.valueOf(qReq.getQuestionType()));
                        question.setStemType(StemType.valueOf(qReq.getStemType()));
                        question.setStemRefId(qReq.getStemRefId());
                        question.setStemText(qReq.getStemText());
                        question.setDifficulty(1);
                        question.setMaxScore(qReq.getMaxScore());
                        question.setCreatedAt(LocalDateTime.now());
                        question.setUpdatedAt(LocalDateTime.now());

                        // Options: assign orders starting from 1 for each new question
                        List<TestOption> opts = new ArrayList<>();
                        int optionOrder = 1;
                        for (var oReq : qReq.getOptions()) {
                            TestOption option = new TestOption();
                            option.setQuestion(question);
                            option.setContentType(ContentType.valueOf(oReq.getContentType()));
                            option.setContentRefId(oReq.getContentRefId());
                            option.setText(oReq.getText());
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
                        question.setOptions(opts);

                        // Save question (cascade will save options if configured)
                        testQuestionRepository.save(question);
                    }

                    // Build response DTO
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
                // Likely a unique constraint violation on (test_id, orderInTest)
                if (attempt >= MAX_RETRY) {
                    throw new BadRequestException("Khong tao duoc test question (conflict orderIndex) sau " + MAX_RETRY + " lan thu.");
                }
                // short backoff to reduce collision chance
                try {
                    Thread.sleep(RETRY_SLEEP_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                // then retry
            } catch (Exception exception) {
                // lỗi khác -> ném BadRequest
                throw new BadRequestException("Khong tao duoc test: " + exception.getMessage());
            }
        }
    }

    //lấy Test theo testId
    public TestRes getTestById(Long id){
        Test test = testRepository.findById(id).orElseThrow(()->new NotFoundException("Not found test: "+id));
        TestRes testRes = new TestRes();
        testRes.setId(test.getId());
        testRes.setLessonId(test.getLessonTest().getId());
        testRes.setTitle(test.getTitle());
        testRes.setType(test.getTestType().toString());
        testRes.setDescription(test.getDescription());
        testRes.setDurationMin(test.getDurationMin());
        testRes.setActive(test.getActive());

        List<TestQuestion> testQuestionList = testQuestionRepository.findByTestIdOrderByOrderInTest(test.getId());
        List<TestQuestionRes> testQuestionRes = new ArrayList<>();
        for(TestQuestion question:testQuestionList){
            TestQuestionRes questionRes = new TestQuestionRes();
            questionRes.setId(question.getId());
            questionRes.setQuestionType(question.getQuestionType().toString());
            if(question.getStemType().equals(StemType.IMAGE)||question.getStemType().equals(StemType.AUDIO)){
                MediaAsset mediaAsset = mediaAssetRepository.findById(question.getStemRefId())
                        .orElseThrow(()-> new NotFoundException("Not found mediaAsset for test question :"+question.getStemRefId()));
                questionRes.setMediaUrl(mediaAsset.getUrl());
            }
            //check xem co StemText hay khong
            if(question.getStemText()==null){
                questionRes.setQuestionContent(null);
            }else{
                questionRes.setQuestionContent(question.getStemText());
            }
            questionRes.setDifficult(question.getDifficulty());
            questionRes.setMaxScore(question.getMaxScore());
            questionRes.setPosition(question.getOrderInTest());
            //set options cho question
            List<TestOption> testOptionList = testOptionRepository.findByQuestionIdOrderByOrder(question.getId());
            List<TestOptionRes> testOptionResList = new ArrayList<>();
            for(TestOption option:testOptionList){
                TestOptionRes optionRes = new TestOptionRes();
                optionRes.setId(option.getId());
                ContentType ct = option.getContentType(); // có thể null

                if (ct == null) {
                    // nếu contentType null, fallback dùng text (hoặc xử lý khác tuỳ yêu cầu)
                    optionRes.setOptionText(option.getText());
                } else {
                    switch (ct) {
                        case VOCAB -> {
                            Vocabulary vocabulary = vocabularyRepository.findById(option.getContentRefId())
                                    .orElseThrow(() -> new NotFoundException("Not found vocab for test option: " + option.getContentRefId()));
                            optionRes.setOptionText(vocabulary.getTerm_en());
                        }
                        case SENTENCE -> {
                            Sentence sentence = sentenceRepository.findById(option.getContentRefId())
                                    .orElseThrow(() -> new NotFoundException("Not found sentence for test option: " + option.getContentRefId()));
                            optionRes.setOptionText(sentence.getSentence_en());
                        }
                        case IMAGE -> {
                            MediaAsset mediaAsset = mediaAssetRepository.findById(option.getContentRefId())
                                    .orElseThrow(() -> new NotFoundException("Not found media for test option: " + option.getContentRefId()));
                            optionRes.setOptionText(mediaAsset.getUrl());
                        }
                        default -> optionRes.setOptionText(option.getText());
                    }
                }
                optionRes.setCorrect(option.isCorrect());
                optionRes.setPosition(option.getOrder());
                if(option.getSide()==null){
                    optionRes.setSide(null);
                }else{
                    optionRes.setSide(option.getSide().toString());
                }
                optionRes.setPairKey(option.getPairKey());
                testOptionResList.add(optionRes);
            }
            questionRes.setOptions(testOptionResList);
            testQuestionRes.add(questionRes);
        }
        testRes.setQuestionRes(testQuestionRes);
        return testRes;
    }

    public SubmitTestRes submitAndGrade(Long learnerId, Long testId, SubmitTestReq req) {
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
            //lấy câu trả lời theo questionId
            AnswerReq a = byQ.get(q.getId()); // có thể null → coi như bỏ qua

            //lấy các option của câu hỏi
            List<TestOption> opts = optsByQ.getOrDefault(q.getId(), List.of());

            //lấy điểm của câu hỏi
            int maxScore = Optional.ofNullable(q.getMaxScore()).orElse(1);

            //truyền vào câu hỏi, các option và đáp án của người học để kiểm tra kết quả
            GradeResult gr = gradeOne(q, opts, a); // tính đúng/sai + điểm + đúng là gì
            // Lưu TestAnswer theo loại câu:
            persistAnswer(attempt, q, a, gr, opts);

            if (gr.correct()) {
                correct++;
                totalScore += gr.earnedScore();
            } else {
                wrong++;
            }
            // GỌI LOGIC MỚI (luôn luôn gọi)
            incorrectItemLogService.logTestOptions(
                    learner.getId(),
                    test.getLessonTest().getId(),
                    q,
                    opts,
                    gr.correct() // 👈 Truyền kết quả
            );


            QuestionResultRes resultRes = new QuestionResultRes();
            resultRes.setQuestionId(q.getId());
            resultRes.setQuestionType(q.getQuestionType().toString());
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

            resultRes.setCorrectOptionIds(gr.correctOptionIds());
            //lấy danh sách correctOptionIds truy vấn danh sách các contentRefId để lấy nội dung của option, chứ không lấy id của option
//            List<Long> contentRefIds = testOptionRepository.findContentRefIdsOrderByInput(gr.correctOptionIds());
            List<Long> contentRefIds = testOptionRepository.findContentRefIdsOrderByInput(gr.correctOptionIds().toArray(new Long[0]));
            details.add(resultRes);
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
                // so sánh đủ cặp
//                Set<String> correctPairs = opts.stream()
//                        .filter(o -> o.getSide() == null) // bỏ qua option đơn lẻ
//                        .map(o -> o.getId().toString())   // (nếu bạn mã hoá pair khác, sửa lại)
//                        .collect(Collectors.toSet());
                // Gợi ý: lưu đáp án đúng kiểu (leftId->rightId) qua pairKey hoặc bảng phụ
                // Ở đây bạn tuỳ chỉnh theo cách lưu đáp án đúng
                boolean ok = checkMatchingExactly(a, opts);
                return new GradeResult(ok, ok ? maxScore : 0.0, correctIds);
            }
            case WORD_TO_SENTENCE -> {
                boolean ok = checkOrderingExactly(a, opts);
                return new GradeResult(ok, ok ? maxScore : 0.0, correctIds);
            }
            case SENTENCE_HIDDEN_WORD ,PICTURE_WORD_WRITING-> {
                String gold = correctTextFromOptions(opts); // bạn định nghĩa: lấy option.correct=true -> text
                boolean ok = (a != null && normalize(a.getTextInput()).equals(normalize(gold)));
                return new GradeResult(ok, ok ? maxScore : 0.0, List.of());
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
        return opts.stream().filter(TestOption::isCorrect).map(TestOption::getText).findFirst().orElse("");
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
        if(testList==null){
            throw new NotFoundException("Bài học này chưa có bài kiểm tra");
        }

        List<TestResByLesson> testResList = new ArrayList<>();
        for(Test test:testList){
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
        return testResList;
    }

    public List<LessonWithTestsRes> getTestsByGradeId(Long gradeId) {
        // 1. Lấy tất cả các bài test thuộc grade đó
        List<Test> tests = testRepository.findByLessonTest_GradeLevel_IdOrderByLessonTest_OrderIndexAscCreatedAtAsc(gradeId);

        if (tests.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Gom nhóm các Test theo Lesson (Key: Lesson, Value: List<Test>)
        // Lưu ý: Lesson cần override equals/hashCode chuẩn hoặc dùng ID để gom nhóm nếu Lesson entity chưa tối ưu
        // Ở đây dùng LinkedHashMap để giữ thứ tự query (đã sort theo orderIndex)
        Map<Long, List<Test>> testsByLessonMap = tests.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getLessonTest().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<LessonWithTestsRes> result = new ArrayList<>();

        // 3. Duyệt map để tạo DTO response
        for (Map.Entry<Long, List<Test>> entry : testsByLessonMap.entrySet()) {
            List<Test> testGroup = entry.getValue();
            if (testGroup.isEmpty()) continue;

            // Lấy thông tin Lesson từ phần tử đầu tiên trong nhóm
            Lesson lesson = testGroup.get(0).getLessonTest();

            LessonWithTestsRes lessonRes = new LessonWithTestsRes();
            lessonRes.setLessonId(lesson.getId());
            lessonRes.setUnitName(lesson.getUnitName());
            lessonRes.setLessonName(lesson.getLessonName());
            lessonRes.setOrderIndex(lesson.getOrderIndex());

            // Map danh sách Test entity sang TestResByLesson DTO
            List<TestResByLesson> testDtos = testGroup.stream().map(t -> {
                TestResByLesson dto = new TestResByLesson();
                dto.setId(t.getId());
                dto.setLessonId(t.getLessonTest().getId());
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



}



