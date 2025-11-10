package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonLockStatusRes {
    private boolean vocabLearned;       // Đã học xong tất cả VOCAB
    private boolean vocabGamesDone;     // Đã chơi xong tất cả VOCAB_GAMES
    private boolean sentenceLearned;    // Đã học xong tất cả SENTENCE
    private boolean sentenceGamesDone;  // Đã chơi xong tất cả SENTENCE_GAMES
    private boolean allTestsDone;
}
