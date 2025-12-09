package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyStatRes {
    private String date; // Format: "dd/MM" hoặc "yyyy-MM-dd"
    private long count;  // Số lượng bài học hoàn thành
}
