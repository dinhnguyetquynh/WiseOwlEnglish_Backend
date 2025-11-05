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


}


