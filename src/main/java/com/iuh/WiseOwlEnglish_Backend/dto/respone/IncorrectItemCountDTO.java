package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncorrectItemCountDTO {
    private Long itemRefId; // ID của Vocab hoặc Sentence
    private Long wrongCount;  // Tổng số lần sai (còn nợ)
}
