package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLearnerProfileReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.LearnerProfileReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LearnerProfileRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.ProfileByLearnerRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.ProfileRes;
import com.iuh.WiseOwlEnglish_Backend.service.LearnerProfileService;
import com.iuh.WiseOwlEnglish_Backend.service.MyUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/learner-profiles")
@RequiredArgsConstructor
public class LearnerProfileController {
    private final LearnerProfileService service;

    @PostMapping("/me")
    public ResponseEntity<LearnerProfileRes> createMine(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody LearnerProfileReq req
    ) {
        // Trong filter, bạn set Authentication principal là UserDetails với username = email
        String email = principal.getUsername();

        LearnerProfileRes res = service.createForCurrentUserByEmail(email, req);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(res.getId())
                .toUri();

        return ResponseEntity.created(location).body(res);
    }

    @GetMapping("list-by-user")
    public ResponseEntity<List<LearnerProfileRes>> getListProfilesByUserId(@AuthenticationPrincipal MyUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<LearnerProfileRes> resList = service.getLearnerProfilesByUserId(userId);
        return ResponseEntity.ok(resList);
    }

    @PostMapping("/create-profile")
    public ResponseEntity<ProfileRes> createProfile(@AuthenticationPrincipal MyUserDetails userDetails, @Validated @RequestBody CreateLearnerProfileReq req) {
        Long userId = userDetails.getId();
        ProfileRes res = service.createProfile(req, userId);
        return ResponseEntity.status(201).body(res);

    }

    @GetMapping("/get-profile/{id}")
    public ResponseEntity<LearnerProfileRes> getProfileById(@PathVariable("id") Long id){
        LearnerProfileRes profileRes = service.getLearnerProfile(id);
        return ResponseEntity.ok(profileRes);
    }
    @GetMapping("/{id}")
    public  ResponseEntity<ProfileByLearnerRes> getProfileByLearner(@PathVariable("id") Long learnerId){
        ProfileByLearnerRes res = service.getProfileByLearner(learnerId);
        return ResponseEntity.ok(res);
    }
}
