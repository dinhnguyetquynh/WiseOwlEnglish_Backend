package com.iuh.WiseOwlEnglish_Backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iuh.WiseOwlEnglish_Backend.dto.request.*;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.enums.*;

import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.GameCreationException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.exception.ResourceAlreadyExistsException;
import com.iuh.WiseOwlEnglish_Backend.mapper.GameMapper;
import com.iuh.WiseOwlEnglish_Backend.mapper.GameMapperIf;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.iuh.WiseOwlEnglish_Backend.enums.GameType.*;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final GameOptionRepository gameOptionRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;
    private final GameMapper gameMapper;
    private final GameMapperIf gameMapperIf;

    private final GameAttemptRepository gameAttemptRepository;
    private final GameAnswerRepository gameAnswerRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final ObjectMapper objectMapper;

    private final IncorrectItemLogService incorrectItemLogService;
    //FUNCTION FOR LEARNER
    public List<PictureGuessingGameRes> getListGamePictureGuessing(long lessonId){
        Optional<Long> gameId = gameRepository.findGameIdByTypeAndLessonId(PICTURE_WORD_MATCHING,lessonId);
        if(gameId.isPresent()){
            List<GameQuestion> gameQuestions = gameQuestionRepository.findByGameId(gameId.get());
            return gameQuestions.stream().map(gameQuestion -> {
                PictureGuessingGameRes res = new PictureGuessingGameRes();
                res.setId(gameQuestion.getId());
                res.setGameId(gameQuestion.getGame().getId());
                res.setPosition(gameQuestion.getPosition());
                res.setReward(gameQuestion.getRewardCore());
                if(gameQuestion.getPromptType().toString().equals("IMAGE")){
                    MediaAsset media = mediaAssetRepository.findById(gameQuestion.getPromptRefId()).orElse(null);
                    res.setImageUrl(media != null ? media.getUrl() : null);
                }

                List<GameOption> options = gameOptionRepository.findByGameQuestionId(gameQuestion.getId());
                List<PictureGuessingGameOptionRes> listOptions = options.stream().map(option -> {
                    PictureGuessingGameOptionRes optionRes = new PictureGuessingGameOptionRes();
                    optionRes.setId(option.getId());
                    if(option.getContentType().toString().equals(("VOCAB"))){
                        Vocabulary vocabulary = vocabularyRepository.findById(option.getContentRefId()).orElse(null);
                        optionRes.setOptionText(vocabulary != null ? vocabulary.getTerm_en() : null);
                    }
                    optionRes.setCorrect(option.isCorrect());
                    optionRes.setPosition(option.getPosition());
                    return optionRes;
                }).toList();
                res.setOptions(listOptions);
                return res;
            }).toList();
        }
        return List.of();
    };

    public List<SoundWordQuestionRes> getListGameSoundWord(long lessonId){
        Optional<Long> gameId = gameRepository.findGameIdByTypeAndLessonId(SOUND_WORD_MATCHING,lessonId);
        if(gameId.isPresent()){
            List<GameQuestion> gameQuestions = gameQuestionRepository.findByGameId(gameId.get());
            return gameQuestions.stream().map(gameQuestion -> {
                SoundWordQuestionRes res = new SoundWordQuestionRes();
                res.setGameId(gameId.get());
                res.setId(gameQuestion.getId());
                res.setPosition(gameQuestion.getPosition());
                res.setRewardPoint(gameQuestion.getRewardCore());
                MediaAsset media = mediaAssetRepository.findById(gameQuestion.getPromptRefId()).orElse(null);
                res.setUrlSound(media != null ? media.getUrl() : null);

                List<GameOption> options = gameOptionRepository.findByGameQuestionId(gameQuestion.getId());
                List<SoundWordOptionRes> listOptions = options.stream().map(option -> {
                    SoundWordOptionRes optionRes = new SoundWordOptionRes();

                    optionRes.setId(option.getId());
                    optionRes.setGameQuestionId(gameQuestion.getId());
                    if(option.getContentType().toString().equals(("VOCAB"))){
                        Vocabulary vocabulary = vocabularyRepository.findById(option.getContentRefId()).orElse(null);
                        optionRes.setOptionText(vocabulary != null ? vocabulary.getTerm_en() : null);
                    }
                    optionRes.setCorrect(option.isCorrect());
                    optionRes.setPosition(option.getPosition());
                    return optionRes;
                }).toList();
                res.setOptions(listOptions);
                return res;
            }).toList();
        }
        return List.of();
    };

    public List<PictureSentenceQuesRes> getListGamePictureSentence(long lessonId){
        System.out.println("READY GET GAME");
        Optional<Long> gameId = gameRepository.findGameIdByTypeAndLessonId(PICTURE_SENTENCE_MATCHING,lessonId);
        if(gameId.isPresent()){
            List<GameQuestion> gameQuestions = gameQuestionRepository.findByGameId(gameId.get());
            return gameQuestions.stream().map(gameQuestion -> {
                PictureSentenceQuesRes res = new PictureSentenceQuesRes();
                res.setId(gameQuestion.getId());
                res.setGameId(gameId.get());
                res.setPosition(gameQuestion.getPosition());

                res.setSentenceQues(gameQuestion.getQuestionText());
                MediaAsset mediaAsset = mediaAssetRepository.findById(gameQuestion.getPromptRefId())
                        .orElseThrow(()-> new NotFoundException("Not found img for question"));
                res.setImageUrl(mediaAsset.getUrl());
                res.setRewardPoint(gameQuestion.getRewardCore());


                List<GameOption> options = gameOptionRepository.findByGameQuestionId(gameQuestion.getId());
                List<PictureSentenceOptRes> listOptions = options.stream().map(option -> {
                    PictureSentenceOptRes optionRes = new PictureSentenceOptRes();

                    optionRes.setId(option.getId());
                    optionRes.setQuestionId(gameQuestion.getId());
                    optionRes.setSentenceAnswer(option.getAnswerText());
                    optionRes.setCorrect(option.isCorrect());
                    optionRes.setPosition(option.getPosition());
                    return optionRes;
                }).toList();
                res.setOptions(listOptions);
                return res;
            }).toList();
        }
        return List.of();
    };

    public List<PictureWordRes> getListGamePictureWordWriting(long lessonId){
        System.out.println("READY GET GAME WORD WRITING");
        Long gameId = gameRepository.findGameIdByTypeAndLessonId(PICTURE_WORD_WRITING,lessonId)
                .orElseThrow(()->new NotFoundException("Bai hoc nay chua co game nhin hin viet tu vung"));
        if(gameId!=null){
            List<GameQuestion> questionList = gameQuestionRepository.findByGameId(gameId);

            List<PictureWordRes> resList = new ArrayList<>();

            for(GameQuestion question: questionList){
                PictureWordRes questionRes = new PictureWordRes();
                questionRes.setId(question.getId());
                questionRes.setGameId(gameId);
                questionRes.setPosition(question.getPosition());
                MediaAsset mediaAsset = mediaAssetRepository.findById(question.getPromptRefId())
                        .orElseThrow(()-> new NotFoundException("Khong tim thay hinh anh cho cau hoi")); //img cua vocab
                questionRes.setImgURL(mediaAsset.getUrl());
                questionRes.setRewardCore(question.getRewardCore());

                List<GameOption> options = gameOptionRepository.findByGameQuestionId(question.getId());
                List<PictureWordOptRes> optResList = new ArrayList<>();
                for(GameOption option: options){
                    PictureWordOptRes optRes = new PictureWordOptRes();
                    optRes.setId(option.getId());
                    optRes.setQuestionId(option.getGameQuestion().getId());
                    optRes.setAnswerText(option.getAnswerText());
                    optRes.setPosition(option.getPosition());
                    optRes.setCorrect(option.isCorrect());
                    optResList.add(optRes);
                }
                questionRes.setOptsRes(optResList);
                resList.add(questionRes);
            }
            return resList;
        }
        return List.of();
    }

    public List<PictureMatchWordRes> getGamesPictureMatchWord(long lessonId){
        System.out.println("READY GET GAME WORD WRITING");
        Long gameId = gameRepository.findGameIdByTypeAndLessonId(PICTURE4_WORD4_MATCHING,lessonId)
                .orElseThrow(()->new NotFoundException("Bai hoc nay chua co game NOI HINH VA TU VUNG"));
        if(gameId!=null){
            List<GameQuestion> questionList = gameQuestionRepository.findByGameId(gameId);

            List<PictureMatchWordRes> resList = new ArrayList<>();

            for(GameQuestion question: questionList){
                PictureMatchWordRes questionRes = new PictureMatchWordRes();
                questionRes.setId(question.getId());
                questionRes.setGameId(gameId);
                questionRes.setPosition(question.getPosition());
                questionRes.setRewardCore(question.getRewardCore());

                List<GameOption> options = gameOptionRepository.findByGameQuestionId(question.getId());
                List<PictureMatchWordOptRes> optResList = new ArrayList<>();
                for(GameOption option: options){
                    PictureMatchWordOptRes optRes = new PictureMatchWordOptRes();
                    optRes.setId(option.getId());
                    optRes.setQuestionId(option.getGameQuestion().getId());

                    if(option.getContentRefId()!=null){
                        MediaAsset mediaAsset = mediaAssetRepository.findById(option.getContentRefId())
                                .orElseThrow(()-> new NotFoundException("Khong tim thay hinh anh co id: "+option.getContentRefId()));
                        optRes.setImgUrl(mediaAsset.getUrl());
                    }
                    optRes.setSide(option.getSide().toString());
                    optRes.setPairKey(option.getPairKey());
                    optRes.setAnswerText(option.getAnswerText());
                    optRes.setPosition(option.getPosition());
                    optRes.setCorrect(option.isCorrect());
                    optResList.add(optRes);
                }
                questionRes.setOptRes(optResList);
                resList.add(questionRes);
            }
            return resList;
        }
        return List.of();

    }

    public List<SentenceHiddenRes> getGamesSentenceHidden(long lessonId){
        System.out.println("READY GET GAME SENTENCE HIDDEN WORD");
        Long gameId = gameRepository.findGameIdByTypeAndLessonId(SENTENCE_HIDDEN_WORD,lessonId)
                .orElseThrow(()->new NotFoundException("Bai hoc nay chua co game DIEN TU CON THIEU TRONG CAU"));
        if(gameId!=null){
            List<GameQuestion> questionList = gameQuestionRepository.findByGameId(gameId);

            List<SentenceHiddenRes> resList = new ArrayList<>();

            for(GameQuestion question: questionList){
                SentenceHiddenRes questionRes = new SentenceHiddenRes();
                questionRes.setId(question.getId());
                questionRes.setGameId(gameId);
                questionRes.setPosition(question.getPosition());
                if(question.getPromptRefId()!=null){
                    MediaAsset mediaAsset = mediaAssetRepository.findById(question.getPromptRefId())
                            .orElseThrow(()->new NotFoundException("Khong tim duoc hinh anh co id:"+question.getPromptRefId()));
                    questionRes.setImgURL(mediaAsset.getUrl());
                }
                questionRes.setQuestionText(question.getQuestionText());
                questionRes.setHiddenWord(question.getHiddenWord());
                questionRes.setRewardCore(question.getRewardCore());

                List<GameOption> options = gameOptionRepository.findByGameQuestionId(question.getId());
                List<SentenceHiddenOptRes> optResList = new ArrayList<>();
                for(GameOption option: options){
                    SentenceHiddenOptRes optRes = new SentenceHiddenOptRes();
                    optRes.setId(option.getId());
                    optRes.setQuestionId(option.getGameQuestion().getId());
                    optRes.setAnswerText(option.getAnswerText());
                    optRes.setPosition(option.getPosition());
                    optRes.setCorrect(option.isCorrect());
                    optResList.add(optRes);
                }
                questionRes.setOptRes(optResList);
                resList.add(questionRes);
            }
            return resList;
        }
        return List.of();

    }

    public List<WordToSentenceRes> getWordToSentenceGame(long lessonId){
        System.out.println("READY WORD TO SENTENCE GAME");
        Long gameId = gameRepository.findGameIdByTypeAndLessonId(WORD_TO_SENTENCE,lessonId)
                .orElseThrow(()->new NotFoundException("Bai hoc nay chua co game SAP XEP TU THANH CAU"));
        if(gameId!=null){
            List<GameQuestion> questionList = gameQuestionRepository.findByGameId(gameId);

            List<WordToSentenceRes> resList = new ArrayList<>();

            for(GameQuestion question: questionList){
                WordToSentenceRes questionRes = new WordToSentenceRes();
                questionRes.setId(question.getId());
                questionRes.setGameId(gameId);
                questionRes.setPosition(question.getPosition());
                questionRes.setQuestionText(question.getQuestionText());
                questionRes.setRewardCore(question.getRewardCore());

                List<GameOption> options = gameOptionRepository.findByGameQuestionId(question.getId());
                List<WordToSentenceOptsRes> optResList = new ArrayList<>();
                for(GameOption option: options){
                    WordToSentenceOptsRes optRes = new WordToSentenceOptsRes();
                    optRes.setId(option.getId());
                    optRes.setQuestionId(option.getGameQuestion().getId());
                    optRes.setAnswerText(option.getAnswerText());
                    optRes.setPosition(option.getPosition());
                    optRes.setCorrect(option.isCorrect());
                    optResList.add(optRes);
                }
                questionRes.setOpts(optResList);
                resList.add(questionRes);
            }
            return resList;
        }
        return List.of();

    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<GameResByLesson> getGamesForReview(Long lessonId, String category) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new NotFoundException("Lesson not found: " + lessonId);
        }

        // Xác định danh sách GameType cần lấy dựa trên category
        Collection<GameType> gameTypesToFetch;
        if ("vocab".equalsIgnoreCase(category)) {
            gameTypesToFetch = GameType.VOCAB_GAMES; // Dùng Set static từ enum
        } else if ("sentence".equalsIgnoreCase(category)) {
            gameTypesToFetch = GameType.SENTENCE_GAMES; // Dùng Set static từ enum
        } else {
            // Nếu category không hợp lệ, trả về danh sách rỗng
            return Collections.emptyList();
        }

        // Gọi phương thức repo mới
        List<Game> games = gameRepository.findByLesson_IdAndTypeIn(lessonId, gameTypesToFetch);

        // Map danh sách Game sang DTO GameResByLesson
        return gameMapperIf.gamesToGameResByLessons(games);
    }

    @Transactional
    public GameAnswerRes submitAnswer(GameAnswerReq req) {
        // 1. Lấy các đối tượng cần thiết
        GameQuestion question = gameQuestionRepository.findById(req.getGameQuestionId())
                .orElseThrow(() -> new NotFoundException("GameQuestion not found"));

        Game game = question.getGame(); // Lấy game từ question
        if (!game.getId().equals(req.getGameId())) {
            throw new BadRequestException("GameID không khớp với GameQuestionID");
        }

        LearnerProfile profile = learnerProfileRepository.getReferenceById(req.getProfileId());

        // 2. Tìm hoặc Tạo GameAttempt (lần chơi)
        GameAttempt attempt = gameAttemptRepository
                .findByLearnerProfile_IdAndGame_IdAndStatus(
                        req.getProfileId(),
                        req.getGameId(),
                        AttemptStatus.IN_PROGRESS // Chỉ tìm cái đang chơi
                )
                .orElseGet(() -> createNewAttempt(profile, game)); // Tạo mới nếu không có cái nào đang chơi

        // 3. Chấm điểm (Logic chính)
        List<GameOption> options = gameOptionRepository.findByGameQuestionId(req.getGameQuestionId());
        GraderResult result = gradeOneQuestion(question, options, req);

        // 4. Tạo và Lưu GameAnswer (lưu vết)
        GameAnswer answer = new GameAnswer();
        answer.setAttempt(attempt);
        answer.setGameQuestion(question);
        answer.setCorrect(result.isCorrect());
        answer.setCreatedAt(LocalDateTime.now());

        // (Lưu lại chi tiết câu trả lời của người dùng)
        saveAnswerDetails(answer, req, options);

        gameAnswerRepository.save(answer);

        // 5. Cập nhật GameAttempt
        attempt.setUpdatedAt(LocalDateTime.now());
        attempt.setCurrentQuestionId(question.getId()); // Cập nhật câu hỏi vừa trả lời
        if (result.isCorrect()) {
            attempt.setRewardCount(attempt.getRewardCount() + result.reward());
        } else {
            attempt.setWrongCount(attempt.getWrongCount() + 1);
        }
        // GỌI LOGIC MỚI (luôn luôn gọi)
        incorrectItemLogService.logGameOptions(
                profile.getId(),
                game.getLesson().getId(),
                question,
                options,
                result.isCorrect() // 👈 Truyền kết quả
        );

        // ========== BẮT ĐẦU LOGIC MỚI ==========
        // 5.1. Kiểm tra xem đây có phải là câu hỏi cuối cùng không

        // Lấy tất cả câu hỏi của game này, theo đúng thứ tự
        List<GameQuestion> allQuestionsInGame = gameQuestionRepository
                .findByGameIdOrderByPositionAsc(game.getId());

        if (!allQuestionsInGame.isEmpty()) {
            // Lấy câu hỏi cuối cùng trong danh sách
            GameQuestion lastQuestion = allQuestionsInGame.get(allQuestionsInGame.size() - 1);

            // So sánh ID của câu hiện tại với ID của câu cuối cùng
            if (question.getId().equals(lastQuestion.getId())) {
                // Nếu đây LÀ câu cuối cùng, chuyển trạng thái
                attempt.setStatus(AttemptStatus.COMPLETED);

                // (Nên thêm trường 'completedAt' vào GameAttempt để lưu thời gian hoàn thành)
                // attempt.setCompletedAt(LocalDateTime.now());

                System.out.println("Lượt chơi " + attempt.getId() + " đã HOÀN THÀNH!");
            }
        }
        gameAttemptRepository.save(attempt);
        System.out.println("KET QUA CUA CAU 1 LA :"+result.isCorrect);
        // 6. Trả kết quả về cho FE
        return GameAnswerRes.builder()
                .isCorrect(result.isCorrect())
                .correctAnswerText(result.correctAnswerText())
                .rewardEarned(result.reward())
                .build();
    }

    private GameAttempt createNewAttempt(LearnerProfile profile, Game game) {
        GameAttempt attempt = GameAttempt.builder()
                .learnerProfile(profile)
                .game(game)
                .status(AttemptStatus.IN_PROGRESS)
                .rewardCount(0) // Khởi tạo là 0
                .wrongCount(0) // Khởi tạo là 0
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return gameAttemptRepository.save(attempt);
    }

    // Record nội bộ để chứa kết quả chấm
    private record GraderResult(boolean isCorrect, int reward, String correctAnswerText) {}

    // Hàm chấm điểm cho 1 câu hỏi
    private GraderResult gradeOneQuestion(GameQuestion question, List<GameOption> options, GameAnswerReq req) {
        int reward = question.getRewardCore();

        try {
            switch (question.getGame().getType()) {
                // Game chọn 1 (PICTURE_WORD_MATCHING, SOUND_WORD_MATCHING, PICTURE_SENTENCE_MATCHING)
                case PICTURE_WORD_MATCHING:
                case SOUND_WORD_MATCHING:
                case PICTURE_SENTENCE_MATCHING: {
                    GameOption correctOpt = options.stream().filter(GameOption::isCorrect).findFirst().orElse(null);
                    if (correctOpt == null) return new GraderResult(false, 0, "[Lỗi cấu hình game]");

                    boolean isCorrect = correctOpt.getId().equals(req.getOptionId());
                    String correctText = getOptionText(correctOpt); // Helper để lấy text (từ vocab, sentence, hoặc text)

                    return new GraderResult(isCorrect, isCorrect ? reward : 0, correctText);
                }

                // Game điền từ (PICTURE_WORD_WRITING, SENTENCE_HIDDEN_WORD)
                case PICTURE_WORD_WRITING:
                case SENTENCE_HIDDEN_WORD: {
                    GameOption correctOpt = options.stream().filter(GameOption::isCorrect).findFirst().orElse(null);
                    if (correctOpt == null) return new GraderResult(false, 0, "[Lỗi cấu hình game]");

                    String correctAnswer = correctOpt.getAnswerText();
                    boolean isCorrect = normalize(req.getTextInput()).equals(normalize(correctAnswer));
                    return new GraderResult(isCorrect, isCorrect ? reward : 0, correctAnswer);
                }

                // Game nối cặp (PICTURE4_WORD4_MATCHING)
                case PICTURE4_WORD4_MATCHING: {
                    if (req.getPairs() == null || req.getPairs().isEmpty()) return new GraderResult(false, 0, "Nối các cặp");

                    Map<Long, String> pairKeyMap = options.stream()
                            .collect(Collectors.toMap(GameOption::getId, GameOption::getPairKey));

                    boolean allMatch = true;
                    // Đảm bảo số cặp nối = số cặp đúng (thường là 4)
                    int correctPairCount = (int) options.stream().map(GameOption::getPairKey).distinct().count();
                    if (req.getPairs().size() != correctPairCount) allMatch = false;

                    if (allMatch) {
                        for (PairDTO pair : req.getPairs()) {
                            String leftKey = pairKeyMap.get(pair.getLeftOptionId());
                            String rightKey = pairKeyMap.get(pair.getRightOptionId());
                            if (leftKey == null || !leftKey.equals(rightKey)) {
                                allMatch = false;
                                break;
                            }
                        }
                    }
                    return new GraderResult(allMatch, allMatch ? reward : 0, "Nối đúng " + correctPairCount + " cặp");
                }

                // Game sắp xếp (WORD_TO_SENTENCE)
                case WORD_TO_SENTENCE: {
                    if (req.getSequence() == null || req.getSequence().isEmpty()) return new GraderResult(false, 0, "Sắp xếp câu");

                    // Lấy chuỗi ID đúng theo 'position'
                    List<Long> correctSequence = options.stream()
                            .sorted(Comparator.comparing(GameOption::getPosition))
                            .map(GameOption::getId)
                            .toList();

                    boolean isCorrect = correctSequence.equals(req.getSequence());

                    String correctAnswer = options.stream()
                            .sorted(Comparator.comparing(GameOption::getPosition))
                            .map(GameOption::getAnswerText)
                            .collect(Collectors.joining(" ")); // Nối bằng khoảng trắng

                    return new GraderResult(isCorrect, isCorrect ? reward : 0, correctAnswer);
                }

                default:
                    return new GraderResult(false, 0, "Không hỗ trợ loại game này");
            }
        } catch (Exception e) {
            return new GraderResult(false, 0, "Lỗi chấm điểm: " + e.getMessage());
        }
    }

    // Helper lưu chi tiết câu trả lời vào GameAnswer
    private void saveAnswerDetails(GameAnswer answer, GameAnswerReq req, List<GameOption> options) {
        // 1. Lưu câu trả lời dạng text
        if (req.getTextInput() != null) {
            answer.setAnswerText(req.getTextInput());
        }

        // 2. Lưu câu trả lời dạng chọn 1
        if (req.getOptionId() != null) {
            options.stream()
                    .filter(o -> o.getId().equals(req.getOptionId()))
                    .findFirst()
                    .ifPresent(answer::setOption); // setOption(GameOption)
        }

        // 3. (Nâng cao) Lưu câu trả lời dạng nối cặp
        if (req.getPairs() != null && !req.getPairs().isEmpty()) {
            // Chúng ta có thể lưu nó dưới dạng JSON
            answer.setAnswerText(writeJson(req.getPairs()));
        }

        // 4. (Nâng cao) Lưu câu trả lời dạng sắp xếp
        if (req.getSequence() != null && !req.getSequence().isEmpty()) {
            answer.setAnswerText(writeJson(req.getSequence()));
        }
    }

    // Helper lấy text từ GameOption (vì nó có thể là vocab, sentence, text)
    private String getOptionText(GameOption opt) {
        if (opt.getAnswerText() != null) return opt.getAnswerText();
        if (opt.getContentType() == ContentType.VOCAB && opt.getContentRefId() != null) {
            return vocabularyRepository.findById(opt.getContentRefId()).map(Vocabulary::getTerm_en).orElse("");
        }
        if (opt.getContentType() == ContentType.SENTENCE && opt.getContentRefId() != null) {
            return sentenceRepository.findById(opt.getContentRefId()).map(Sentence::getSentence_en).orElse("");
        }
        return "";
    }

    // Helper chuẩn hóa text
    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT);
    }

    // Helper chuyển object sang JSON
    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return null; // Hoặc ném RuntimeException
        }
    }


}


