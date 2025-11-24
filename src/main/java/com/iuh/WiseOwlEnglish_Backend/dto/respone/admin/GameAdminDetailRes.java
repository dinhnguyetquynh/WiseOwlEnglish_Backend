package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import com.iuh.WiseOwlEnglish_Backend.dto.request.GameQuestionReq;
import lombok.Data;

import java.util.List;

@Data
public class GameAdminDetailRes {
    private Long id;
    private String title;
    private String type;
    private int difficulty;
    private Long lessonId;
    private boolean active;
    private List<GameQuestionRes> questions; // Tận dụng lại DTO Req cho tiện, hoặc tạo Res riêng nếu cần field ID
}
