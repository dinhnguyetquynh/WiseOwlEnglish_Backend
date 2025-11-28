package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class ShopDataRes {
    private int currentBalance;
    private List<CategoryGroupDto> categories;
    private List<Long> ownedStickerIds;

    @Data
    public static class CategoryGroupDto {
        private Long id;
        private String name;
        private List<StickerItemDto> stickers;
    }

    @Data
    public static class StickerItemDto {
        private Long id;
        private String name;
        private String imageUrl;
        private int price;
        private String rarity;
    }
}
