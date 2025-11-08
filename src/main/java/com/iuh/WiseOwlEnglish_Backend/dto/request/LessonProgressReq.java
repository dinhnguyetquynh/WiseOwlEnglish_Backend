package com.iuh.WiseOwlEnglish_Backend.dto.request;

import com.iuh.WiseOwlEnglish_Backend.enums.ItemType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonProgressReq {
    @NotNull
    private Long learnerProfileId;

    @NotNull
    private Long lessonId;

    @NotNull
    private ItemType itemType; // Sẽ tự động map từ "VOCAB", "SENTENCE", v.v.

    @NotNull
    private Long itemRefId;
}
