package com.iuh.WiseOwlEnglish_Backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GameAnswerReq {
    @NotNull
    private Long profileId;

    @NotNull
    private Long gameId; // ID của Game (ví dụ: Game "Nối từ vựng" của Lesson 1)

    @NotNull
    private Long gameQuestionId; // ID của câu hỏi cụ thể

    // Dùng cho các game chọn 1 đáp án (PICTURE_WORD_MATCHING, SOUND_WORD_MATCHING)
    private Long optionId;

    // Dùng cho game điền từ (PICTURE_WORD_WRITING, SENTENCE_HIDDEN_WORD)
    private String textInput;

    // Dùng cho game nối (PICTURE4_WORD4_MATCHING)
    // (Chúng ta sẽ dùng lại PairDTO từ Test)
    private List<PairDTO> pairs;

    // Dùng cho game sắp xếp câu (WORD_TO_SENTENCE)
    private List<Long> sequence; // Mảng các optionId theo thứ tự
}
