package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.TestReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TestRes;
import com.iuh.WiseOwlEnglish_Backend.enums.*;
import com.iuh.WiseOwlEnglish_Backend.model.Lesson;
import com.iuh.WiseOwlEnglish_Backend.model.Test;
import com.iuh.WiseOwlEnglish_Backend.model.TestOption;
import com.iuh.WiseOwlEnglish_Backend.model.TestQuestion;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    //ADMIN FUNCTIONALITY
    @Transactional
    public TestRes createTest(TestReq request) {
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

        Test savedTest = testRepository.save(test); // ✅ test có ID rồi

        for (var qReq : request.getQuestions()) {
            TestQuestion question = new TestQuestion();
            question.setTest(savedTest);
            question.setQuestionType(TestQuestionType.valueOf(qReq.getQuestionType()));
            question.setStemType(StemType.valueOf(qReq.getStemType()));
            question.setStemRefId(qReq.getStemRefId());
            question.setStemText(qReq.getStemText());
            question.setDifficulty(qReq.getDifficulty());
            question.setMaxScore(qReq.getMaxScore());
            question.setOrderInTest(qReq.getOrderInTest());
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());

            List<TestOption> opts = new ArrayList<>();
            for (var oReq : qReq.getOptions()) {
                TestOption option = new TestOption();
                option.setQuestion(question);                      // set side owning
                option.setContentType(ContentType.valueOf(oReq.getContentType()));
                option.setContentRefId(oReq.getContentRefId());
                option.setText(oReq.getText());
                option.setCorrect(oReq.isCorrect());
                option.setOrder(oReq.getOrder());
                if (oReq.getSide() != null) {
                    option.setSide(Side.valueOf(oReq.getSide()));  // chỉ set khi không null
                }
                option.setPairKey(oReq.getPairKey());
                option.setCorrectOrder(oReq.getOrder());
                option.setCreatedAt(LocalDateTime.now());
                option.setUpdatedAt(LocalDateTime.now());
                opts.add(option);
            }
            question.setOptions(opts);

            testQuestionRepository.save(question); // ✅ cascade ALL sẽ tự persist options
            // KHÔNG gọi testOptionRepository.save(option) ở trên nữa
        }

        TestRes res = new TestRes();
        res.setId(savedTest.getId());
        res.setLessonId(savedTest.getLessonTest().getId());
        res.setActive(savedTest.getActive());
        res.setTitle(savedTest.getTitle());
        res.setType(savedTest.getTestType().toString());
        res.setDescription(savedTest.getDescription());
        res.setDurationMin(savedTest.getDurationMin());
        return res;
    }

}
