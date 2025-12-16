package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.RankingRes;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerProfileRepository;
import com.iuh.WiseOwlEnglish_Backend.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<RankingRes> getGlobalRanking(
            @RequestParam Long profileId
    ) {


        RankingRes response = rankingService.getGlobalRanking(profileId);
        return ResponseEntity.ok(response);
    }
}
