package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.*;
import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.enums.MediaType;
import com.iuh.WiseOwlEnglish_Backend.model.*;
import com.iuh.WiseOwlEnglish_Backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final GameOptionRepository gameOptionRepository;
    private final MediaAssetRepository mediaAssetRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SentenceRepository sentenceRepository;

    public List<PictureGuessingGameRes> getListGamePictureGuessing(long lessonId){
        Optional<Long> gameId = gameRepository.findGameIdByTypeAndLessonId(GameType.PICTURE_WORD_MATCHING,lessonId);
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
        Optional<Long> gameId = gameRepository.findGameIdByTypeAndLessonId(GameType.SOUND_WORD_MATCHING,lessonId);
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
        Optional<Long> gameId = gameRepository.findGameIdByTypeAndLessonId(GameType.PICTURE_SENTENCE_MATCHING,lessonId);
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


}
