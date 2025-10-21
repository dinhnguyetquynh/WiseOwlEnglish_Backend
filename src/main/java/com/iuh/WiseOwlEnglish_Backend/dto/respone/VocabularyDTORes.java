package com.iuh.WiseOwlEnglish_Backend.dto.respone;

import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyDTORes {
    private Long id;
    private String term_en;
    private String term_vi;
    private String phonetic;
    private int orderIndex;   // 1..n để sắp xếp thứ tự
    private String partOfSpeech;
    private List<MediaAssetDTORes> mediaAssets; // <- dùng DTO thay vì entity
}
