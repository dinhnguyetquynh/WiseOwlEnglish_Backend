package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.GameOptionReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.GameQuestionReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.GameReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.enums.*;

import com.iuh.WiseOwlEnglish_Backend.exception.GameCreationException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.exception.ResourceAlreadyExistsException;
import com.iuh.WiseOwlEnglish_Backend.mapper.GameMapper;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.iuh.WiseOwlEnglish_Backend.enums.GameType.*;

@Service
@RequiredArgsConstructor
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final GameOptionRepository gameOptionRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;
    private final GameMapper gameMapper;



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
        Optional<Long> gameId = gameRepository.findGameIdByTypeAndLessonId(PICTURE_SENTENCE_MATCHING,lessonId);
        if(gameId.isPresent()){
            List<GameQuestion> gameQuestions = gameQuestionRepository.findByGameId(gameId.get());
            return gameQuestions.stream().map(gameQuestion -> {
                PictureSentenceQuesRes res = new PictureSentenceQuesRes();
                res.setId(gameQuestion.getId());
                res.setGameId(gameId.get());
                res.setPosition(gameQuestion.getPosition());
                Sentence sentence = sentenceRepository.findById(gameQuestion.getPromptRefId()).orElse(null);
                res.setSentenceQues(sentence != null ? sentence.getSentence_en(): null);
                String imgUrl = mediaAssetRepository.findImageUrlBySentenceId(sentence.getId(), MediaType.IMAGE).orElse(null);
                res.setImageUrl(imgUrl);
                res.setRewardPoint(gameQuestion.getRewardCore());


                List<GameOption> options = gameOptionRepository.findByGameQuestionId(gameQuestion.getId());
                List<PictureSentenceOptRes> listOptions = options.stream().map(option -> {
                    PictureSentenceOptRes optionRes = new PictureSentenceOptRes();

                    optionRes.setId(option.getId());
                    optionRes.setQuestionId(gameQuestion.getId());
                    if(option.getContentType().toString().equals(("SENTENCE"))){
                        Sentence optionSentence = sentenceRepository.findById(option.getContentRefId()).orElse(null);
                        optionRes.setSentenceAnswer(optionSentence != null ? optionSentence.getSentence_en() : null);
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

    //FUNCTION FOR ADMIN
    // add new game
    public GameRes createGame(GameReq req){
        if (req.getType() == null) {
            throw new IllegalArgumentException("Loại game không được để trống");
        }

        switch (GameType.valueOf(req.getType())) {
            case PICTURE_WORD_MATCHING:
                return createPictureWordMatching(req);
            case SOUND_WORD_MATCHING:
                return createSoundWordMatching(req);
            case PICTURE_SENTENCE_MATCHING:
                return createPictureSentenceMatching(req);
            case PICTURE_WORD_WRITING:
                return createPictureWordWriting(req);
            case PICTURE4_WORD4_MATCHING:
                return createPicture4Word4Matching(req);
            case SENTENCE_HIDDEN_WORD:
                return createSentenceHiddenWord(req);
            case WORD_TO_SENTENCE:
                return createWordToSentence(req);
            default:
                throw new IllegalArgumentException("Loại game không được hỗ trợ: " + req.getType());
        }

    }

    public Game createNewGame(GameReq req){
        try {
            boolean gameExists = gameRepository.existsByTypeAndLessonId(GameType.valueOf(req.getType()), req.getLessonId());
            if (gameExists) {
                throw new ResourceAlreadyExistsException("Loại game :"+req.getType()+"đã tồn tại trong lesson " +req.getLessonId());
            }
            Game newGame = new Game();
            newGame.setTitle(req.getTitle());
            newGame.setType(GameType.valueOf(req.getType()));
            newGame.setDifficulty(req.getDifficulty());
            // java
            Lesson lesson = lessonRepository.findById(req.getLessonId())
                    .orElseThrow(() -> new NotFoundException("Lesson not found: " + req.getLessonId()));
            newGame.setLesson(lesson);
            MediaAsset correctAudio = mediaAssetRepository.findById(req.getCorrectAudioId())
                    .orElseThrow(() -> new NotFoundException("Correct sound not found: " + req.getCorrectAudioId()));
            newGame.setCorrectAudio(correctAudio);

            MediaAsset wrongAudio = mediaAssetRepository.findById(req.getWrongAudioId())
                    .orElseThrow(() -> new NotFoundException("Wrong sound not found: " + req.getWrongAudioId()));
            newGame.setWrongAudio(wrongAudio);
            newGame.setCreatedAt(LocalDateTime.now());
            newGame.setUpdatedAt(LocalDateTime.now());
            Game savedGame = gameRepository.save(newGame);
            return savedGame;

        }catch (ResourceAlreadyExistsException e){
            throw e;

        } catch (Exception exception){
            System.err.println("Lỗi nghiêm trọng khi tạo game: " + exception.getMessage());
            exception.printStackTrace(); // Rất quan trọng để log stack trace
            throw new GameCreationException("Tạo game thất bại. Vui lòng thử lại sau.");
        }

    }

    //Game dành cho lớp 1 : nhìn hình chọn chữ.
    public GameRes createPictureWordMatching(GameReq req){
        try {
            Game savedGame = createNewGame(req);

            //create ListQuestion
            for(var question:req.getQuestions()){
                GameQuestion gameQuestion = new GameQuestion();
                gameQuestion.setGame(savedGame);
                gameQuestion.setPosition(question.getPosition());
                gameQuestion.setPromptType(PromptType.valueOf(question.getPromptType()));
                gameQuestion.setPromptRefId(question.getPromptRefId());
                gameQuestion.setRewardCore(question.getRewardCore());
                gameQuestion.setCreatedAt(LocalDateTime.now());
                gameQuestion.setUpdatedAt(LocalDateTime.now());

                List<GameOption> listOptions = new ArrayList<>();
                for(var option : question.getOptionReqs()){
                    GameOption opt = new GameOption();
                    opt.setGameQuestion(gameQuestion);
                    opt.setContentType(ContentType.valueOf(option.getContentType()));
                    opt.setContentRefId(option.getContentRefId());
                    opt.setCorrect(option.isCorrect());
                    opt.setPosition(option.getPosition());
                    opt.setCreatedAt(LocalDateTime.now());
                    opt.setUpdatedAt(LocalDateTime.now());
                    listOptions.add(opt);

                }
                gameQuestion.setOptions(listOptions);
                GameQuestion savedQuestion = gameQuestionRepository.save(gameQuestion);
            }

            GameRes res = gameMapper.toDTO(savedGame);
            return res;
        }catch (GameCreationException exception) {
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game thất bại. Vui lòng thử lại sau.");

        }
    }

    //Game dành cho lớp 1: nghe âm thanh chọn hình
    public GameRes createSoundWordMatching(GameReq req){
        try {
            //CHECK

            Game savedGame = createNewGame(req);

            for(GameQuestionReq questionReq : req.getQuestions()){
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPosition(questionReq.getPosition());
                question.setPromptType(PromptType.valueOf(questionReq.getPromptType()));
                question.setPromptRefId(questionReq.getPromptRefId());
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //create and set list options for question
                List<GameOption> gameOptionList = new ArrayList<>();
                for(GameOptionReq optionReq:questionReq.getOptionReqs()){
                    GameOption option = new GameOption();
                    option.setGameQuestion(question);
                    option.setContentType(ContentType.valueOf(optionReq.getContentType()));
                    option.setContentRefId(optionReq.getContentRefId());
                    option.setCorrect(optionReq.isCorrect());
                    option.setPosition(optionReq.getPosition());
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());

                    gameOptionList.add((option));
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            return gameMapper.toDTO(savedGame);

        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game thất bại. Vui lòng thử lại sau.");

        }

    }

    //Game dành cho lớp 1: nhìn hình chọn câu
    public GameRes createPictureSentenceMatching(GameReq req){
        try {
            Game savedGame = createNewGame(req);

            for(GameQuestionReq questionReq : req.getQuestions()){
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPosition(questionReq.getPosition());
                question.setPromptType(PromptType.valueOf(questionReq.getPromptType()));
                question.setPromptRefId(questionReq.getPromptRefId());
                question.setQuestionText(questionReq.getQuestionText());
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //create and set list options for question
                List<GameOption> gameOptionList = new ArrayList<>();
                for(GameOptionReq optionReq:questionReq.getOptionReqs()){
                    GameOption option = new GameOption();
                    option.setGameQuestion(question);
                    option.setAnswerText(optionReq.getAnswerText());
                    option.setCorrect(optionReq.isCorrect());
                    option.setPosition(optionReq.getPosition());
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());
                    gameOptionList.add((option));
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            return gameMapper.toDTO(savedGame);


        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game nhìn hình chọn câu thất bại. Vui lòng thử lại sau.");
        }
    }

    //game dành cho lớp 3: nhìn hình vieet từ vựng
    public GameRes createPictureWordWriting(GameReq req){
        try {
            Game savedGame = createNewGame(req);

            for(GameQuestionReq questionReq : req.getQuestions()){
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPosition(questionReq.getPosition());
                question.setPromptType(PromptType.valueOf(questionReq.getPromptType()));
                question.setPromptRefId(questionReq.getPromptRefId());
                question.setQuestionText(questionReq.getQuestionText());
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //create and set list options for question
                List<GameOption> gameOptionList = new ArrayList<>();
                for(GameOptionReq optionReq:questionReq.getOptionReqs()){
                    GameOption option = new GameOption();
                    option.setGameQuestion(question);
                    option.setAnswerText(optionReq.getAnswerText());
                    option.setCorrect(optionReq.isCorrect());
                    option.setPosition(optionReq.getPosition());
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());
                    gameOptionList.add((option));
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            return gameMapper.toDTO(savedGame);
        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game nhìn hình  và viết từ vựng thất bại. Vui lòng thử lại sau.");
        }
    }

    //Game dành cho lớp 3: nối hình và chữ
    public GameRes createPicture4Word4Matching(GameReq req){
        try {
            Game savedGame = createNewGame(req);

            for(GameQuestionReq questionReq : req.getQuestions()){
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPosition(questionReq.getPosition());
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //game have 6 options
                List<GameOption> gameOptionList = new ArrayList<>();
                for(GameOptionReq optionReq:questionReq.getOptionReqs()){
                    GameOption option = new GameOption();
                    option.setGameQuestion(question);
                    if(optionReq.getContentType()==null){
                        option.setContentType(null);
                    }else{
                        option.setContentType(ContentType.valueOf(optionReq.getContentType()));
                    }

                    if (optionReq.getContentRefId()==null){
                        option.setContentRefId(null);
                    }else {
                        option.setContentRefId(optionReq.getContentRefId());
                    }
                    if(optionReq.getAnswerText()==null){
                        option.setAnswerText(null);
                    }else {
                        option.setAnswerText(optionReq.getAnswerText());
                    }
                    option.setSide(Side.valueOf(optionReq.getSide()));
                    option.setPairKey(optionReq.getPairKey());
                    option.setCorrect(optionReq.isCorrect());
                    option.setPosition(optionReq.getPosition());
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());
                    gameOptionList.add((option));
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            return gameMapper.toDTO(savedGame);
        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game Nối hình ảnh với từ vựng thất bại. Vui lòng thử lại sau.");
        }
    }

    //Game dành cho lớp 3: điền từ còn thiếu trong câu
    public GameRes createSentenceHiddenWord(GameReq req){
        try {
            Game savedGame = createNewGame(req);

            // 3.3) Tạo câu đã che từ: replace lần đầu bằng "___"

            for(GameQuestionReq questionReq : req.getQuestions()){
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPromptType(PromptType.valueOf(questionReq.getPromptType()));
                question.setPromptRefId(questionReq.getPromptRefId());

                String full = questionReq.getQuestionText();
                String hidden = questionReq.getHiddenWord();
                if (!containsLoose(full, hidden)) {
                    throw new GameCreationException("hiddenWord không khớp với câu: " + hidden);
                }
                String masked = maskFirstOccurrence(full, hidden, "___");
                question.setQuestionText(masked);
                question.setHiddenWord(questionReq.getHiddenWord());
                question.setPosition(questionReq.getPosition());
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //game have 1 option
                    GameOption option = new GameOption();
                    option.setGameQuestion(question);
                    option.setAnswerText(questionReq.getHiddenWord());
                    option.setCorrect(true);
                    option.setPosition(1);
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());


                question.setOptions(List.of(option));
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            return gameMapper.toDTO(savedGame);
        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game điền từ còn thiếu trong câu thất bại. Vui lòng thử lại sau.");
        }
    }

    //Game dành cho lớp 3: sắp xếp từ thành câu
    public GameRes createWordToSentence(GameReq req){
        //admin tạo câu , backend split câu thành các tokens và lưu mỗi token thành 1 option
        try {
            Game savedGame = createNewGame(req);

            for (GameQuestionReq questionReq : req.getQuestions()) {
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setQuestionText(questionReq.getQuestionText());
                question.setPosition(questionReq.getPosition());
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                List<String> tokens = tokenizeKeepPunct(questionReq.getQuestionText());
                System.out.println("LIST TOKEN LA :" + tokens.toString());

                int pos=1;
                List<GameOption> gameOptionList = new ArrayList<>();
                for (String tk : tokens) {
                    GameOption opt = new GameOption();
                    opt.setGameQuestion(question);
                    opt.setAnswerText(tk);   // hiển thị lên FE
                    opt.setPosition(pos++);  // 1,2,3,...
                    opt.setCreatedAt(LocalDateTime.now());
                    opt.setUpdatedAt(LocalDateTime.now());
                    gameOptionList.add(opt);
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            return gameMapper.toDTO(savedGame);
        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game Sắp xếp từ thành câu thất bại. Vui lòng thử lại sau.");
        }
    }

    /**
     * Tách câu thành token và GIỮ dấu câu là token riêng.
     * Đủ dùng cho lớp 3: xử lý . , ! ? ; : và ngoặc kép/ngoặc đơn cơ bản.
     * Ví dụ: "She is reading a book." -> ["She","is","reading","a","book","."]
     */
    private List<String> tokenizeKeepPunct(String sentence) {
        if (sentence == null || sentence.isBlank()) return List.of();

        // Chèn khoảng trắng quanh các dấu phổ biến để split
        String spaced = sentence
                .replaceAll("([.,!?;:])", " $1 ")
                .replaceAll("([()\"“”‘’])", " $1 ")   // ngoặc và dấu trích dẫn
                .replaceAll("\\s+", " ")              // gọn khoảng trắng
                .trim();

        String[] parts = spaced.split(" ");
        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            if (!p.isBlank()) tokens.add(p);
        }
        return tokens;
    }

    private String maskFirstOccurrence(String sentence, String word, String placeholder) {
        // (?i) bỏ qua hoa/thường
        String regex = "(?i)" + Pattern.quote(word.trim());
        return sentence.replaceFirst(regex, placeholder);
    }
    private boolean containsLoose(String sentence, String word) {
        String a = normalize(sentence);
        String b = normalize(word);
        return a.contains(b);
    }
    private String normalize(String s) {
        String t = s.toLowerCase(Locale.ROOT).trim();
        // Bỏ dấu tiếng Việt:
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}


