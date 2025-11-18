package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import lombok.Data;

@Data
public class VocabRes {
    private long id;
    private int orderIndex;
    private String term_en;
    private String phonetic;
    private String partOfSpeech;
}
