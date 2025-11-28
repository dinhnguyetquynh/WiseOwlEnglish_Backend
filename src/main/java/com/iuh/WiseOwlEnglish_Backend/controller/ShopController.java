package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.ShopDataRes;
import com.iuh.WiseOwlEnglish_Backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    @GetMapping("/{learnerId}")
    public ResponseEntity<ShopDataRes> getShop(@PathVariable Long learnerId) {
        return ResponseEntity.ok(shopService.getShopData(learnerId));
    }

    @PostMapping("/buy")
    public ResponseEntity<Void> buySticker(@RequestParam Long learnerId, @RequestParam Long stickerId) {
        shopService.buySticker(learnerId, stickerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/equip")
    public ResponseEntity<Void> equipSticker(@RequestParam Long learnerId, @RequestParam Long stickerId) {
        shopService.equipSticker(learnerId, stickerId);
        return ResponseEntity.ok().build();
    }
}
