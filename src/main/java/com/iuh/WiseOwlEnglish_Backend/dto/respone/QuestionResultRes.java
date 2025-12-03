package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.iuh.WiseOwlEnglish_Backend.dto.request.PairDTO;
import lombok.Data;

import java.util.List;

@Data
public class QuestionResultRes {
   private Long questionId;
    private String questionType;
    private boolean correct;
    private Double earnedScore;
    private Integer maxScore;
    private Long selectedOptionId;
    private List<Long> selectedOptionIds;
    private List<Long> correctOptionIds;
    private String textInput; // Lưu câu trả lời dạng text của user
    private List<Long> userSequence;    // Cho Word To Sentence
    private List<PairDTO> userPairs;    // Cho Picture Match Word
}
