package com.iuh.WiseOwlEnglish_Backend.dto.request;

import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GameReq {
    @NotNull(message = "Tiêu đề không được để trống")
    private String title;

    @NotNull(message = "Loại game không được để trống")
    private String type;

//    @NotNull(message = "Độ khó không được để trống")
//    private Integer difficulty;

    @NotNull(message = "Lesson ID không được để trống")
    private Long lessonId;

    @NotNull(message = "Danh sách câu hỏi không được để trống")
    private List<@Valid GameQuestionReq> questions;

    private boolean active;
}
