package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.iuh.WiseOwlEnglish_Backend.enums.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaAssetDTORes {
    private Long id;
    private String url;
    private MediaType mediaType;
    private String altText;
    private Integer durationSec;
    private String storageProvider;
    private String publicId;
    private String tag;

}
