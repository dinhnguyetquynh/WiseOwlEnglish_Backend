package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.GameOptionReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.GameQuestionReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.GameReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.GameAdminDetailRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.GameOptionRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.GameQuestionRes;
import com.iuh.WiseOwlEnglish_Backend.enums.ContentType;
import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.enums.PromptType;
import com.iuh.WiseOwlEnglish_Backend.enums.Side;
import com.iuh.WiseOwlEnglish_Backend.event.LessonContentChangedEvent;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.GameCreationException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.exception.ResourceAlreadyExistsException;
import com.iuh.WiseOwlEnglish_Backend.mapper.GameMapper;
import com.iuh.WiseOwlEnglish_Backend.mapper.GameMapperIf;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GameServiceAdmin {
    private final GameRepository gameRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final GameOptionRepository gameOptionRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;
    private final GameMapper gameMapper;
    private final GameMapperIf gameMapperIf;
    private final DataGameService dataGameService;
    private final GameAttemptRepository gameAttemptRepository;

    private final ApplicationEventPublisher eventPublisher;
    // 👇 1. INJECT CACHE MANAGER
    private final CacheManager cacheManager;

    // 👇 2. VIẾT HÀM HỖ TRỢ XÓA CACHE THỦ CÔNG
    private void clearGameCache(Long lessonId) {
        if (lessonId == null) return;

        Cache cache = cacheManager.getCache("lessonTotals");
        if (cache != null) {
            // Xóa các key liên quan đến việc đếm game
            cache.evict(lessonId + "_vocab_games");
            cache.evict(lessonId + "_sentence_games");
            // Xóa cache đếm tổng câu hỏi (quan trọng)
            cache.evict(lessonId + "_gamequestion");

            System.out.println("🧹 Đã clear cache lessonTotals cho Lesson ID: " + lessonId);
        }
    }

    //FUNCTION FOR ADMIN
    // add new game
    // Xóa cả cache vocab_games và sentence_games khi tạo game mới
    @Caching(evict = {
            @CacheEvict(value = "lessonTotals", key = "#req.lessonId + '_vocab_games'"),
            @CacheEvict(value = "lessonTotals", key = "#req.lessonId + '_sentence_games'")
    })
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
            boolean gameExists = gameRepository.existsByLessonIdAndTypeAndDeletedAtIsNull(req.getLessonId(),GameType.valueOf(req.getType()));
            if (gameExists) {
                throw new ResourceAlreadyExistsException("Loại game :"+req.getType()+"đã tồn tại trong lesson " +req.getLessonId());
            }
            Game newGame = new Game();
            newGame.setTitle(req.getTitle());
            newGame.setType(GameType.valueOf(req.getType()));
            newGame.setDifficulty(1);
            newGame.setActive(req.isActive());
            // java
            Lesson lesson = lessonRepository.findById(req.getLessonId())
                    .orElseThrow(() -> new NotFoundException("Lesson not found: " + req.getLessonId()));
            newGame.setLesson(lesson);

            long correctAudioId = 267;
            MediaAsset correctAudio = mediaAssetRepository.findById(correctAudioId)
                    .orElseThrow(() -> new NotFoundException("Correct sound not found: " + correctAudioId));
            newGame.setCorrectAudio(correctAudio);

            long wrongAudioId = 268;
            MediaAsset wrongAudio = mediaAssetRepository.findById(wrongAudioId)
                    .orElseThrow(() -> new NotFoundException("Wrong sound not found: " + wrongAudioId));
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
            //tự động đánh position cho GameQuestion (bắt đầu từ 1)
            int questionIndex = 0;
            for(var question:req.getQuestions()){
                questionIndex++;
                GameQuestion gameQuestion = new GameQuestion();
                gameQuestion.setGame(savedGame);
                gameQuestion.setPosition(questionIndex);
                //img :
                gameQuestion.setPromptType(PromptType.valueOf(question.getPromptType()));
                gameQuestion.setPromptRefId(question.getPromptRefId());

                gameQuestion.setRewardCore(question.getRewardCore());
                gameQuestion.setCreatedAt(LocalDateTime.now());
                gameQuestion.setUpdatedAt(LocalDateTime.now());

                List<GameOption> listOptions = new ArrayList<>();
                if (question.getOptionReqs() != null) {
                    int optionIndex = 0;
                    for (var option : question.getOptionReqs()) {
                        optionIndex++;
                        GameOption opt = new GameOption();
                        opt.setGameQuestion(gameQuestion);
                        opt.setContentType(ContentType.valueOf(option.getContentType()));
                        opt.setContentRefId(option.getContentRefId());
                        opt.setCorrect(option.isCorrect());
                        opt.setPosition(optionIndex); // <-- set tự động, bỏ qua giá trị client
                        opt.setCreatedAt(LocalDateTime.now());
                        opt.setUpdatedAt(LocalDateTime.now());
                        listOptions.add(opt);
                    }
                }
                gameQuestion.setOptions(listOptions);
                GameQuestion savedQuestion = gameQuestionRepository.save(gameQuestion);
            }

            GameRes res = gameMapper.toDTO(savedGame);
            // 👇 KÍCH HOẠT SỰ KIỆN CHẠY NGẦM 👇
            if (res != null) {
                eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
            }
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
            int questionIndex = 0;
            for(GameQuestionReq questionReq : req.getQuestions()){
                questionIndex++;
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPosition(questionIndex);
                //id sound
                question.setPromptType(PromptType.valueOf(questionReq.getPromptType()));
                question.setPromptRefId(questionReq.getPromptRefId());

                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //create and set list options for question
                List<GameOption> gameOptionList = new ArrayList<>();
                int optionIndex = 0;
                for(GameOptionReq optionReq:questionReq.getOptionReqs()){
                    optionIndex++;
                    GameOption option = new GameOption();
                    option.setGameQuestion(question);
                    //id vocab
                    option.setContentType(ContentType.valueOf(optionReq.getContentType()));
                    option.setContentRefId(optionReq.getContentRefId());

                    option.setCorrect(optionReq.isCorrect());
                    option.setPosition(optionIndex);
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());

                    gameOptionList.add((option));
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            GameRes res= gameMapper.toDTO(savedGame);
            if (res != null) {
                eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
            }
            return res;

        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game thất bại. Vui lòng thử lại sau.");

        }

    }

    //Game dành cho lớp 1: nhìn hình chọn câu
    public GameRes createPictureSentenceMatching(GameReq req){
        try {
            Game savedGame = createNewGame(req);
            int questionIndex = 0;
            for(GameQuestionReq questionReq : req.getQuestions()){
                questionIndex++;
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPosition(questionIndex);
                //id img
                question.setPromptType(PromptType.valueOf(questionReq.getPromptType()));
                question.setPromptRefId(questionReq.getPromptRefId());

//                question.setQuestionText(questionReq.getQuestionText());
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //create and set list options for question
                List<GameOption> gameOptionList = new ArrayList<>();
                int optionIndex = 0;
                for(GameOptionReq optionReq:questionReq.getOptionReqs()){
                    optionIndex++;
                    GameOption option = new GameOption();
                    option.setGameQuestion(question);

                    //id sentence
                    option.setContentType(ContentType.valueOf(optionReq.getContentType()));
                    option.setContentRefId(optionReq.getContentRefId());
                    option.setCorrect(optionReq.isCorrect());
                    option.setPosition(optionIndex);
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());
                    gameOptionList.add((option));
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            GameRes res= gameMapper.toDTO(savedGame);
            if (res != null) {
                eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
            }
            return res;


        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game nhìn hình chọn câu thất bại. Vui lòng thử lại sau.");
        }
    }

    //game dành cho lớp 3: nhìn hình vieet từ vựng
    public GameRes createPictureWordWriting(GameReq req){
        try {
            Game savedGame = createNewGame(req);
            int questionIndex = 0;
            for(GameQuestionReq questionReq : req.getQuestions()){
                questionIndex++;
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPosition(questionIndex);
                //id img
                question.setPromptType(PromptType.valueOf(questionReq.getPromptType()));
                question.setPromptRefId(questionReq.getPromptRefId());

                question.setQuestionText(questionReq.getQuestionText());
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //create and set list options for question
                List<GameOption> gameOptionList = new ArrayList<>();
                int optionIndex = 0;
                for(GameOptionReq optionReq:questionReq.getOptionReqs()){
                    optionIndex++;
                    GameOption option = new GameOption();
                    option.setGameQuestion(question);

                    //id vocab
                    option.setContentType(ContentType.valueOf(optionReq.getContentType()));
                    option.setContentRefId(optionReq.getContentRefId());

                    option.setCorrect(optionReq.isCorrect());
                    option.setPosition(optionIndex);
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());
                    gameOptionList.add((option));
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            GameRes res= gameMapper.toDTO(savedGame);
            if (res != null) {
                eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
            }
            return res;
        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game nhìn hình  và viết từ vựng thất bại. Vui lòng thử lại sau.");
        }
    }

    //Game dành cho lớp 3: nối hình và chữ
    public GameRes createPicture4Word4Matching(GameReq req){
        try {
            Game savedGame = createNewGame(req);

            int questionIndex = 0;
            for(GameQuestionReq questionReq : req.getQuestions()){
                questionIndex++;
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPosition(questionIndex);
                question.setRewardCore(questionReq.getRewardCore());
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());

                //game có 8 options : moi option deu co id cua media hoac vocab
                List<GameOption> gameOptionList = new ArrayList<>();
                int optionIndex = 0;
                for(GameOptionReq optionReq:questionReq.getOptionReqs()){
                    optionIndex++;
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
//                    if(optionReq.getAnswerText()==null){
//                        option.setAnswerText(null);
//                    }else {
//                        option.setAnswerText(optionReq.getAnswerText());
//                    }
                    if(option.getContentType().equals(ContentType.IMAGE)){
                        option.setSide(Side.LEFT);
                    }else {
                        option.setSide(Side.RIGHT);
                    }

                    option.setPairKey(optionReq.getPairKey());
                    option.setCorrect(optionReq.isCorrect());
                    option.setPosition(optionIndex);
                    option.setCreatedAt(LocalDateTime.now());
                    option.setUpdatedAt(LocalDateTime.now());
                    gameOptionList.add((option));
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            GameRes res= gameMapper.toDTO(savedGame);
            if (res != null) {
                eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
            }
            return res;
        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game Nối hình ảnh với từ vựng thất bại. Vui lòng thử lại sau.");
        }
    }

    //Game dành cho lớp 3: điền từ còn thiếu trong câu
    public GameRes createSentenceHiddenWord(GameReq req){
        try {
            Game savedGame = createNewGame(req);

            // 3.3) Tạo câu đã che từ: replace lần đầu bằng "___"
            int questionIndex = 0;
            for(GameQuestionReq questionReq : req.getQuestions()){
                questionIndex++;
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);

                //img hinh anh
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
                question.setPosition(questionIndex);
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
            GameRes res= gameMapper.toDTO(savedGame);
            if (res != null) {
                eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
            }
            return res;
        }catch (GameCreationException exception){
            throw new GameCreationException("Tạo câu hỏi và đáp án cho game điền từ còn thiếu trong câu thất bại. Vui lòng thử lại sau.");
        }
    }

    //Game dành cho lớp 3: sắp xếp từ thành câu
    public GameRes createWordToSentence(GameReq req){
        //admin tạo câu , backend split câu thành các tokens và lưu mỗi token thành 1 option
        try {
            Game savedGame = createNewGame(req);

            int questionIndex = 0;
            for (GameQuestionReq questionReq : req.getQuestions()) {
                questionIndex++;
                GameQuestion question = new GameQuestion();
                question.setGame(savedGame);
                question.setPromptType(PromptType.valueOf(questionReq.getPromptType()));
                question.setPromptRefId(questionReq.getPromptRefId());
                question.setPosition(questionIndex);
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
                    opt.setCorrect(true);
                    opt.setCreatedAt(LocalDateTime.now());
                    opt.setUpdatedAt(LocalDateTime.now());
                    gameOptionList.add(opt);
                }
                question.setOptions(gameOptionList);
                GameQuestion savedQuestion = gameQuestionRepository.save(question);
            }
            GameRes res= gameMapper.toDTO(savedGame);
            if (res != null) {
                eventPublisher.publishEvent(new LessonContentChangedEvent(this, req.getLessonId()));
            }
            return res;
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


    public List<GameResByLesson> getListGameByLesson(long lessonId){
        if (!lessonRepository.existsById(lessonId)) {
            throw new BadRequestException("Lesson not found with id: " + lessonId);
        }
        List<Game> gameList = gameRepository.findByLesson_IdAndDeletedAtIsNull(lessonId);
        List<GameResByLesson> gameResByLessons = gameMapperIf.gamesToGameResByLessons(gameList);
        return gameResByLessons;
    }



    @Transactional(readOnly = true)
    public List<LessonWithGamesDTO> getLessonsWithGamesByGrade(Long gradeId) {
        // 1. Lấy tất cả Lesson thuộc GradeLevel, sắp xếp theo thứ tự
        List<Lesson> lessons = lessonRepository.findByGradeLevel_IdAndDeletedAtIsNullOrderByOrderIndexAsc(gradeId);
        if (lessons.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Lấy danh sách ID của các Lesson
        List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();

        // 3. Lấy tất cả Game thuộc danh sách Lesson ID (chỉ 1 query)
        List<Game> games = gameRepository.findByLesson_IdInAndDeletedAtIsNull(lessonIds);

        // 4. Nhóm các Game theo Lesson ID để tra cứu nhanh
        Map<Long, List<Game>> gamesByLessonIdMap = games.stream()
                .collect(Collectors.groupingBy(game -> game.getLesson().getId()));

        // 5. Ánh xạ sang DTO
        return lessons.stream().map(lesson -> {
            LessonWithGamesDTO lessonDTO = new LessonWithGamesDTO();
            lessonDTO.setLessonId(lesson.getId());
            lessonDTO.setUnitName(lesson.getUnitName());
            lessonDTO.setLessonName(lesson.getLessonName());

            // Lấy danh sách game của lesson này từ Map
            List<Game> lessonGames = gamesByLessonIdMap.getOrDefault(lesson.getId(), Collections.emptyList());

            // Ánh xạ danh sách Game sang GameInfoDTO
            List<GameInfoDTO> gameDTOs = lessonGames.stream()
                    .map(game -> new GameInfoDTO(game.getId(), game.getType().toString()))
                    .collect(Collectors.toList());

            lessonDTO.setGames(gameDTOs);
            return lessonDTO;
        }).collect(Collectors.toList());
    }

    public GamesOfLessonRes getGamesDetailByLesson(long lessonId){
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(()-> new NotFoundException("Khong tim thay lesson co id :"+lessonId));
        List<Game> gameList = gameRepository.findByLesson_IdAndDeletedAtIsNull(lessonId);

        GamesOfLessonRes res = new GamesOfLessonRes();
        res.setLessonId(lesson.getId());
        res.setUnitName(lesson.getUnitName());
        res.setLessonName(lesson.getLessonName());


        List<GameDetailRes> gameDetailResList = new ArrayList<>();
        for(Game game:gameList){
            GameDetailRes detailRes = new GameDetailRes();
            detailRes.setId(game.getId());
            long total = gameQuestionRepository.countByGameId(game.getId());
            detailRes.setTotalQuestion(total);
            detailRes.setGameType(game.getType().toString());
            detailRes.setUpdatedDate(game.getUpdatedAt());
            detailRes.setTitle(game.getTitle());
            detailRes.setActive(game.isActive());
            gameDetailResList.add(detailRes);
        }
        res.setGames(gameDetailResList);
        return res;
    }

    public List<String> getGameTypesByGrade(int gradeOrder,long lessonId) {
        // 1. Chuẩn bị danh sách candidate theo grade
        List<GameType> candidates;
        switch (gradeOrder) {
            case 1, 2:
                candidates = List.of(
                        GameType.PICTURE_WORD_MATCHING,
                        GameType.SOUND_WORD_MATCHING,
                        GameType.PICTURE_SENTENCE_MATCHING,
                        GameType.PICTURE4_WORD4_MATCHING
                );
                break;
            case 3, 4, 5:
                candidates = List.of(
                        GameType.PICTURE_WORD_WRITING,
                        GameType.PICTURE4_WORD4_MATCHING,
                        GameType.SENTENCE_HIDDEN_WORD,
                        GameType.WORD_TO_SENTENCE,
                        GameType.PICTURE_WORD_MATCHING,
                        GameType.SOUND_WORD_MATCHING,
                        GameType.PICTURE_SENTENCE_MATCHING
                );
                break;
            default:
                return Collections.emptyList();
        }

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Query 1 lần: lấy những type trong candidates đã tồn tại cho lessonId
        List<GameType> existed = gameRepository.findTypesByLessonIdAndTypeIn(lessonId, candidates);
        Set<GameType> existedSet = existed == null ? Collections.emptySet() : new HashSet<>(existed);

        // 3. Lọc offline (loại bỏ những type đã tồn tại) và trả về tên enum (String)
        return candidates.stream()
                .filter(g -> !existedSet.contains(g))
                .map(Enum::name)
                .collect(Collectors.toList());
    }



    //  Hàm lấy chi tiết update game
    public GameAdminDetailRes getGameDetailForAdmin(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameId));

        GameAdminDetailRes res = new GameAdminDetailRes();
        res.setId(game.getId());
        res.setTitle(game.getTitle());
        res.setType(game.getType().toString());
        res.setDifficulty(game.getDifficulty());
        res.setLessonId(game.getLesson().getId());
        res.setActive(game.isActive());

        // Lấy danh sách câu hỏi
        List<GameQuestion> questions = gameQuestionRepository.findByGameIdAndDeletedAtIsNullOrderByPositionAsc(gameId);
        List<GameQuestionRes> questionResList = questions.stream().map(q -> {
            GameQuestionRes qReq = new GameQuestionRes();
            // Map dữ liệu câu hỏi
            qReq.setId(q.getId());
            if (game.getType() == GameType.SENTENCE_HIDDEN_WORD && q.getQuestionText() != null && q.getHiddenWord() != null) {
                // Thay thế placeholder (___) bằng hiddenWord
                qReq.setQuestionText(q.getQuestionText().replace("___", q.getHiddenWord()));
            } else {
                qReq.setQuestionText(q.getQuestionText());
            }
//            qReq.setQuestionText(q.getQuestionText());
            qReq.setHiddenWord(q.getHiddenWord());
            qReq.setRewardCore(q.getRewardCore());
            if (q.getPromptType() != null) qReq.setPromptType(q.getPromptType().toString());
            qReq.setPromptRefId(q.getPromptRefId());

            // Map Options
            List<GameOption> options = gameOptionRepository.findByGameQuestionIdAndDeletedAtIsNullOrderByPositionAsc(q.getId());
            List<GameOptionRes> optionResList = options.stream().map(o -> {
                GameOptionRes oReq = new GameOptionRes();
                oReq.setId(o.getId());
                oReq.setAnswerText(o.getAnswerText());
                oReq.setCorrect(o.isCorrect());
                if (o.getContentType() != null) oReq.setContentType(o.getContentType().toString());
                oReq.setContentRefId(o.getContentRefId());
                if (o.getSide() != null) oReq.setSide(o.getSide().toString());
                oReq.setPairKey(o.getPairKey());
                return oReq;
            }).toList();

            qReq.setOptionReqs(optionResList);
            return qReq;
        }).toList();

        res.setQuestions(questionResList);
        return res;
    }

    //HÀM UPDATE GAME
    @Caching(evict = {
            @CacheEvict(value = "lessonTotals", key = "#result.lessonId + '_vocab_games'"), // result là GameRes trả về
            @CacheEvict(value = "lessonTotals", key = "#result.lessonId + '_sentence_games'")
    })
    @Transactional
    public GameRes updateGame(Long gameId, GameReq req) {
        // 1. Cập nhật thông tin chung của Game
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        // 2. KIỂM TRA TRẠNG THÁI ACTIVE
        // Nếu game đang Active (true) => Chặn không cho sửa
        if (game.getLesson().isActive()) {
            throw new BadRequestException("Game đang nằm trong bài học được kích hoạt nên không được cập nhật. Vui lòng tắt kích hoạt bài học rồi cập nhật game.");
        }

        game.setTitle(req.getTitle());
        game.setActive(req.isActive());
        game.setUpdatedAt(LocalDateTime.now());
        // game.setDifficulty(...) // Nếu có update difficulty

        Game savedGame = gameRepository.save(game);

        // 2. Lấy danh sách câu hỏi hiện tại trong DB (chưa bị xóa mềm)
        List<GameQuestion> existingQuestions = gameQuestionRepository.findByGameIdAndDeletedAtIsNullOrderByPositionAsc(gameId);

        // Map để tra cứu nhanh question đang có theo ID
        Map<Long, GameQuestion> existingQuestionMap = existingQuestions.stream()
                .collect(Collectors.toMap(GameQuestion::getId, q -> q));

        // Danh sách ID câu hỏi được gửi lên trong request (để xác định cái nào bị xóa)
        Set<Long> reqQuestionIds = new HashSet<>();

        int qIndex = 0;
        for (var qReq : req.getQuestions()) {
            qIndex++;
            GameQuestion question;

            // A. XỬ LÝ CÂU HỎI (QUESTION)
            if (qReq.getId()!=null && existingQuestionMap.containsKey(qReq.getId())) {
                // --- TRƯỜNG HỢP 1: UPDATE CÂU HỎI CŨ ---
                question = existingQuestionMap.get(qReq.getId());
                reqQuestionIds.add(question.getId()); // Đánh dấu là còn tồn tại

                // Update các trường
                question.setUpdatedAt(LocalDateTime.now());
                // Chỉ set lại nếu có thay đổi (hoặc set luôn cũng được)
            } else {
                // --- TRƯỜNG HỢP 2: TẠO CÂU HỎI MỚI ---
                question = new GameQuestion();
                question.setGame(savedGame);
                question.setCreatedAt(LocalDateTime.now());
                question.setUpdatedAt(LocalDateTime.now());
            }

            // Set dữ liệu chung cho cả mới và cũ
            question.setPosition(qIndex); // Cập nhật lại vị trí mới nhất
            if (qReq.getPromptType() != null)
                question.setPromptType(PromptType.valueOf(qReq.getPromptType()));
            question.setPromptRefId(qReq.getPromptRefId());
            question.setQuestionText(qReq.getQuestionText());
            question.setHiddenWord(qReq.getHiddenWord());
            question.setRewardCore(qReq.getRewardCore());

            // Lưu câu hỏi trước để có ID dùng cho Option (nếu là mới)
            GameQuestion savedQuestion = gameQuestionRepository.save(question);

            // B. XỬ LÝ OPTIONS CỦA CÂU HỎI ĐÓ
            if (qReq.getOptionReqs() != null) {
                updateOptionsForQuestion(savedQuestion, qReq.getOptionReqs());
            }
        }

        // 3. XỬ LÝ CÁC CÂU HỎI BỊ XÓA
        // Những câu hỏi có trong DB nhưng KHÔNG có trong Request -> Cần xóa mềm
        for (GameQuestion existingQ : existingQuestions) {
            if (!reqQuestionIds.contains(existingQ.getId())) {
                // Soft delete câu hỏi
                existingQ.setDeletedAt(LocalDateTime.now());
                gameQuestionRepository.save(existingQ);

                // Soft delete luôn các options con của nó (nếu cần thiết)
                // (Tuỳ logic của bạn, thường xoá cha thì con cũng coi như mất)
            }
        }

        return gameMapper.toDTO(savedGame);
    }

    // Hàm phụ để xử lý Option (tương tự logic câu hỏi)
    private void updateOptionsForQuestion(GameQuestion question, List<GameOptionReq> optionReqs) {
        // Lấy options hiện tại của câu hỏi
        List<GameOption> existingOptions = gameOptionRepository.findByGameQuestionIdAndDeletedAtIsNullOrderByPositionAsc(question.getId());
        Map<Long, GameOption> existingOptionMap = existingOptions.stream()
                .collect(Collectors.toMap(GameOption::getId, o -> o));
        Set<Long> reqOptionIds = new HashSet<>();

        int oIndex = 0;
        for (var oReq : optionReqs) {
            oIndex++;
            GameOption option;

            if (oReq.getId() != null && existingOptionMap.containsKey(oReq.getId())) {
                // UPDATE OPTION CŨ
                option = existingOptionMap.get(oReq.getId());
                reqOptionIds.add(option.getId());
                option.setUpdatedAt(LocalDateTime.now());
            } else {
                // CREATE OPTION MỚI
                option = new GameOption();
                option.setGameQuestion(question);
                option.setCreatedAt(LocalDateTime.now());
                option.setUpdatedAt(LocalDateTime.now());
            }

            // Set dữ liệu
            option.setPosition(oIndex);
            if (oReq.getContentType() != null)
                option.setContentType(ContentType.valueOf(oReq.getContentType()));
            option.setContentRefId(oReq.getContentRefId());
            option.setAnswerText(oReq.getAnswerText());
            option.setCorrect(oReq.isCorrect());
            if (oReq.getSide() != null)
                option.setSide(Side.valueOf(oReq.getSide()));
            option.setPairKey(oReq.getPairKey());

            gameOptionRepository.save(option);
        }

        // XÓA MỀM CÁC OPTION KHÔNG CÒN TRONG REQUEST
        for (GameOption existingOpt : existingOptions) {
            if (!reqOptionIds.contains(existingOpt.getId())) {
                existingOpt.setDeletedAt(LocalDateTime.now());
                gameOptionRepository.save(existingOpt);
            }
        }
    }
//
    public String deleteGame(Long gameId) {
        // 1. Tìm Game
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found with id: " + gameId));
        Long lessonId = game.getLesson().getId(); // Lưu lại ID để dùng cho Event
        // 2. Kiểm tra điều kiện
        boolean isLessonActive = game.getLesson().isActive();

        boolean hasAttempts = gameAttemptRepository.existsByGame_Id(gameId);
        String message;
        // 3. Xử lý phân nhánh
        if (isLessonActive){
            throw new BadRequestException("Game nằm trong bài học đang được kích hoạt nên không được xoá. Vui lòng tắt kích hoạt bài học!");
        }
        else if (!hasAttempts) {
            gameRepository.delete(game);
            message = "Đã xoá vĩnh viễn Game (Hard Delete) vì chưa có dữ liệu người dùng.";
        } else {
            LocalDateTime now = LocalDateTime.now();

            // Ẩn Game
            game.setDeletedAt(now);
            game.setActive(false);

            // Ẩn các câu hỏi con (Question)
            if (game.getQuestions() != null) {
                for (GameQuestion q : game.getQuestions()) {
                    if (q.getDeletedAt() == null) {
                        q.setDeletedAt(now);
                        // Ẩn các đáp án con (Option) của câu hỏi
                        if (q.getOptions() != null) {
                            for (GameOption o : q.getOptions()) {
                                if (o.getDeletedAt() == null) {
                                    o.setDeletedAt(now);
                                }
                            }
                        }
                    }
                }
            }

            gameRepository.save(game);
            message = "Đã xoá mềm Game (Soft Delete) để bảo toàn lịch sử người chơi.";
        }
        // 4. QUAN TRỌNG: Xóa cache và Bắn event tính lại tiến độ
        clearGameCache(lessonId); // Xóa cache thủ công
        eventPublisher.publishEvent(new LessonContentChangedEvent(this, lessonId));

        return message;
    }



}
