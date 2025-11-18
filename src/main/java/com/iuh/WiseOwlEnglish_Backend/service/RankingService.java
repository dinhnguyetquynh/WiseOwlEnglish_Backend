package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.RankItem;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.RankingRes;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerProfile;
import com.iuh.WiseOwlEnglish_Backend.repository.GameAttemptRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final GameAttemptRepository gameAttemptRepository;
    private final LearnerProfileRepository learnerProfileRepository;

    public RankingRes getGlobalRanking(Long currentProfileId) {
        final int TOP_N = 20; // Lấy Top 20

        // 1. Lấy thông tin user hiện tại (bao gồm cả khi 0 điểm)
        LearnerProfile profile = learnerProfileRepository.findById(currentProfileId)
                .orElseThrow(() -> new NotFoundException("LearnerProfile not found: " + currentProfileId));

        RankItem currentUserRank = gameAttemptRepository.findScoreByProfileId(currentProfileId)
                .orElse(new RankItem(currentProfileId, profile.getNickName(), profile.getAvatarUrl(), 0L, 0)); // Fallback

        // 2. Lấy Top N
        Pageable topNPage = PageRequest.of(0, TOP_N);
        List<RankItem> topRanks = gameAttemptRepository.findGlobalRanking(topNPage);

        // Gán rank 1, 2, 3...
        for (int i = 0; i < topRanks.size(); i++) {
            topRanks.get(i).setRank(i + 1);
        }

        // 3. Tìm rank thực tế của user
        if (currentUserRank.getTotalScore() > 0) {
            // Đếm số người có điểm cao hơn
            long higherCount = gameAttemptRepository.countUsersWithScoreGreaterThan(currentUserRank.getTotalScore());
            currentUserRank.setRank((int) higherCount + 1);
        } else {
            currentUserRank.setRank(0); // 0 điểm = chưa xếp hạng
        }

        // 4. Đóng gói Response
        RankingRes response = new RankingRes();
        response.setTopRanks(topRanks);
        response.setCurrentUserRank(currentUserRank);

        return response;
    }
}
