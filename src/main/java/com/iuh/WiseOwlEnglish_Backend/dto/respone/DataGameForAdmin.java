package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import lombok.Data;

import java.util.List;

@Data
public class DataGameForAdmin {
    private String title;
    private List<MediaAssetForAdminDto> mediaAssets;
    private List<OptionsRes> options;
}
