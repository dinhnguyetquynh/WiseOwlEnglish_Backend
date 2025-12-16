package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.GradeLevelDTO;
import com.iuh.WiseOwlEnglish_Backend.service.GradeLevelService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grade-levels")
public class GradeLevelController {
    private final GradeLevelService gradeLevelService;

    public GradeLevelController(GradeLevelService gradeLevelService) {
        this.gradeLevelService = gradeLevelService;
    }

    @GetMapping("/get-all")
    public List<GradeLevelDTO> getAllGradeLevels() {
        return gradeLevelService.getAllGradeLevels();
    }
}