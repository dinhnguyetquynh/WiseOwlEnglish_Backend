package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

@Data
public class StickerRes {
    private Long id;
    private String name;
    private String imgUrl;
    private int price;
    private String rarity;
    private String category;
}
