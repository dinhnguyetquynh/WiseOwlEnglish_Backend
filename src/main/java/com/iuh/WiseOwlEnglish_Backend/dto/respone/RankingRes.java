package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;
@Data
public class RankingRes {
    private List<RankItem> topRanks; // Danh sách Top N (ví dụ Top 20)
    private RankItem currentUserRank; // Thông tin của người dùng đang xem
}
