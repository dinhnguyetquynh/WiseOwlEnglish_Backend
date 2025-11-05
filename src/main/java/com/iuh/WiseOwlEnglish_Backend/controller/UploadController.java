package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final CloudinaryService cloudinaryService;
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User principal
    ) {
        System.out.println("ĐÃ GOI API UPLOAD ANH");
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File rỗng"));
        }
        String url = cloudinaryService.uploadImage(file,"foruser/avatar");
        return ResponseEntity.ok(Map.of("url", url));
    }
}
