package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class SentenceRes {
    private Long id;
    private int orderIndex;   // 1..n để sắp xếp thứ tự
    private String sentence_en;
    private String sentence_vi;
}
