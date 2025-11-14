package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncorrectItemRes {
    private String itemEn;
    private String itemVi;
    private long wrongCount;
}
