package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.ShopDataRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.StickerRes;
import com.iuh.WiseOwlEnglish_Backend.model.Sticker;
import com.iuh.WiseOwlEnglish_Backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    @GetMapping("/{learnerId}")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<ShopDataRes> getShop(@PathVariable Long learnerId) {
        return ResponseEntity.ok(shopService.getShopData(learnerId));
    }

    @PostMapping("/buy")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<Void> buySticker(@RequestParam Long learnerId, @RequestParam Long stickerId) {
        shopService.buySticker(learnerId, stickerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/equip")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<Void> equipSticker(@RequestParam Long learnerId, @RequestParam Long stickerId) {
        shopService.equipSticker(learnerId, stickerId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/reward-epic")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<StickerRes> rewardEpicSticker(@RequestParam Long learnerId) {
        StickerRes sticker = shopService.rewardRandomEpicSticker(learnerId);
        return ResponseEntity.ok(sticker);
    }
}
