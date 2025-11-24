package com.iuh.WiseOwlEnglish_Backend.dto.respone.admin;

import com.iuh.WiseOwlEnglish_Backend.dto.request.GameOptionReq;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GameQuestionRes {
    private long id;
    private String promptType;
    private Long promptRefId;
    private String questionText;
    private String hiddenWord;
    private Integer rewardCore;
    private List<GameOptionRes> optionReqs;
}
