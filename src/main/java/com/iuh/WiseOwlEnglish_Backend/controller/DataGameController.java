package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.DataGameForAdmin;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.MediaAssetForAdminDto;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.OptionsRes;
import com.iuh.WiseOwlEnglish_Backend.enums.GameType;
import com.iuh.WiseOwlEnglish_Backend.service.DataGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data-game")
@RequiredArgsConstructor
public class DataGameController {
    private final DataGameService dataGameService;

    @GetMapping("/get-data")
    public ResponseEntity<DataGameForAdmin> getDataGameForAdmin(
            @RequestParam GameType gameType,
            @RequestParam long lessonId
    ) {

        DataGameForAdmin data = dataGameService.getDataGameForAdmin(gameType, lessonId);

        return ResponseEntity.ok(data);
    }
}
