package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.RankingRes;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerProfileRepository;
import com.iuh.WiseOwlEnglish_Backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;
    private final LearnerProfileRepository learnerProfileRepository; // Để check quyền

    @GetMapping("/global")
    public ResponseEntity<RankingRes> getGlobalRanking(
            @RequestParam Long profileId
    ) {
        // Security check: Đảm bảo profileId này thuộc về user đã đăng nhập
//        boolean isOwner = learnerProfileRepository.findById(profileId)
//                .map(profile -> profile.getUserAccount().getId().equals(userDetails.getId()))
//                .orElse(false);
//
//        if (!isOwner) {
//            throw new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "Bạn không có quyền xem hồ sơ này.");
//        }

        RankingRes response = rankingService.getGlobalRanking(profileId);
        return ResponseEntity.ok(response);
    }
}
