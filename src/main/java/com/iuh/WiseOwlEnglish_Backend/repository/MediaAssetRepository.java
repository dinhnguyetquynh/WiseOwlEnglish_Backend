package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.enums.MediaType;
import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    List<MediaAsset> findBySentenceId(Long sentenceId);
    Optional<MediaAsset> findById(Long id);
    @Query("""
        SELECT m.url 
        FROM MediaAsset m 
        WHERE m.sentence.id = :sentenceId 
          AND m.mediaType = :mediaType
          AND m.deletedAt IS NULL
        """)
    Optional<String> findImageUrlBySentenceId(
            @Param("sentenceId") Long sentenceId,
            @Param("mediaType") MediaType mediaType
    );
}
