package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.MediaAssetForAdminDto;
import com.iuh.WiseOwlEnglish_Backend.enums.MediaType;
import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
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
    @Query("select new com.iuh.WiseOwlEnglish_Backend.dto.respone.MediaAssetForAdminDto(" +
            " m.id, m.url, m.altText, m.tag) " +
            "from MediaAsset m join m.vocabulary v " +
            "where m.mediaType = :mediaType and v.lessonVocabulary.id = :lessonId")
    List<MediaAssetForAdminDto> findAssetVocabByLessonId(@Param("mediaType") MediaType mediaType,
                                                         @Param("lessonId") Long lessonId);

    @Query("select new com.iuh.WiseOwlEnglish_Backend.dto.respone.MediaAssetForAdminDto(" +
            " m.id, m.url, m.altText, m.tag) " +
            "from MediaAsset m join m.sentence s " +
            "where m.mediaType = :mediaType and s.lessonSentence.id = :lessonId")
    List<MediaAssetForAdminDto> findAssetSentenceBySentenceLessonId(@Param("mediaType") MediaType mediaType,
                                                                    @Param("lessonId") Long lessonId);

    // 1. Tìm MediaAsset theo vocabId có mediaType là IMAGE
    // Chúng ta sử dụng List vì một từ vựng có thể có nhiều ảnh (tùy logic của bạn)
    MediaAsset findByVocabularyIdAndMediaType(Long vocabularyId, MediaType mediaType);

    // 2 & 3. Tìm MediaAsset theo vocabId, mediaType và tag (Dùng cho cả 'normal' và 'slow')
    MediaAsset findByVocabularyIdAndMediaTypeAndTag(Long vocabularyId, MediaType mediaType, String tag);
}

