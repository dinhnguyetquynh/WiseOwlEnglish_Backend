package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.TestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestResByLesson;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.LessonWithTestsRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.TestAdminByLessonRes;
import com.iuh.WiseOwlEnglish_Backend.enums.*;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.model.Test;
import com.iuh.WiseOwlEnglish_Backend.model.TestOption;
import com.iuh.WiseOwlEnglish_Backend.model.TestQuestion;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.TestAttemptRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.TestQuestionRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.TestRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestAdminService {
    private final TestRepository testRepository;
    private final TestAttemptRepository attemptRepository;
    private final LessonRepository lessonRepository;
    private final TransactionTemplate transactionTemplate;
    private final TestQuestionRepository testQuestionRepository;

    public List<TestAdminByLessonRes> getTestsByLessonId(Long lessonId) {
        if (lessonId == null) {
            throw new BadRequestException("LessonId đang là null");
        }

        // 1. Lấy danh sách Test theo LessonId
        // (Lưu ý: Nên đảm bảo method này trong Repo đã lọc deletedAt IS NULL như các bước trước)
        List<Test> testList = testRepository.findByLessonTest_Id(lessonId);

        if (testList == null || testList.isEmpty()) {
            return new ArrayList<>();
        }

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

        return testResList;
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
                        question.setHiddenWord(qReq.getHiddenWord());
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
}
