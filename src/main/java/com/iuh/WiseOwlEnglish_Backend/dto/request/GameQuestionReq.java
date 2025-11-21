package com.iuh.WiseOwlEnglish_Backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GameQuestionReq {
    private String promptType;
    private Long promptRefId;
    private String questionText;
    private String hiddenWord;
    @NotNull(message = "RewardCore không được để trống")
    private Integer rewardCore;
    private List<GameOptionReq> optionReqs;

}
