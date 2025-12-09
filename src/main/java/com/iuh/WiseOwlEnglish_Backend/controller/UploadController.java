package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final CloudinaryService cloudinaryService;

    @PostMapping("/lesson/img-mascot")
    public ResponseEntity<Map<String, String>> uploadMascotImg(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadImage(file, "foruser/mascot");
        return ResponseEntity.ok(Map.of("url", url));
    }


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

    @PostMapping("/lesson/audio")
    public ResponseEntity<Map<String, String>> uploadLessonAudio(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadAudio(file, "foruser/lessonAudio");
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/lesson/img")
    public ResponseEntity<Map<String, String>> uploadLessonImg(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadImage(file, "foruser/lessonImg");
        return ResponseEntity.ok(Map.of("url", url));
    }




}
