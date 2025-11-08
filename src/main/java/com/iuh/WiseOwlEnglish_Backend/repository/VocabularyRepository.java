package com.iuh.WiseOwlEnglish_Backend.repository;

import com.iuh.WiseOwlEnglish_Backend.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {
    @Query("""
       SELECT DISTINCT v
       FROM Vocabulary v
       LEFT JOIN FETCH v.mediaAssets
       WHERE v.lessonVocabulary.id = :lessonId
       ORDER BY v.orderIndex ASC, v.id ASC
    """)
    List<Vocabulary> findAllByLessonIdWithAssets(@Param("lessonId") Long lessonId);

    Optional<Vocabulary> findById(long id);

    List<Vocabulary> findByLessonVocabulary_IdAndIsForLearning(Long lessonId, boolean isForLearning);

    long countByLessonVocabulary_IdAndIsForLearning(Long lessonId, boolean isForLearning);
}
