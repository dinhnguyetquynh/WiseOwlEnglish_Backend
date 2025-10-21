package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

@Data
public class PictureGuessingGameOptionRes {
    private Long id;
    private String optionText;
    private boolean correct;
    private int position;
}
