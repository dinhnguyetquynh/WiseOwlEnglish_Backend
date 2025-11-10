package com.iuh.WiseOwlEnglish_Backend.enums;

import java.util.Set;

public enum GameType {
    PICTURE_WORD_MATCHING,
    SOUND_WORD_MATCHING,
    PICTURE_SENTENCE_MATCHING,
    PICTURE_WORD_WRITING,
    PICTURE4_WORD4_MATCHING,
    PRONUNCIATION,
    SENTENCE_HIDDEN_WORD,
    WORD_TO_SENTENCE;

    public static final Set<GameType> VOCAB_GAMES = Set.of(
            PICTURE_WORD_MATCHING,
            SOUND_WORD_MATCHING,
            PICTURE_WORD_WRITING,
            PICTURE4_WORD4_MATCHING
            // (Thêm PRONUNCIATION nếu nó là game từ vựng)
    );

    public static final Set<GameType> SENTENCE_GAMES = Set.of(
            PICTURE_SENTENCE_MATCHING,
            SENTENCE_HIDDEN_WORD,
            WORD_TO_SENTENCE
    );
}
