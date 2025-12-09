package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.StickerRarity;
import com.iuh.WiseOwlEnglish_Backend.model.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, Long> {

    // Thêm hàm này để lấy danh sách Sticker theo độ hiếm
    List<Sticker> findByRarity(StickerRarity rarity);
}
