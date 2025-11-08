package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameDetailRes {
    private Long id;
    private long totalQuestion;
    private String gameType;
    private String title;
    private LocalDateTime updatedDate;
}
