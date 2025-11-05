package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.VocabTestRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.VocabularyDTORes;
import com.iuh.WiseOwlEnglish_Backend.mapper.VocabularyMapper;
import com.iuh.WiseOwlEnglish_Backend.repository.VocabularyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VocabularyService {
    private final VocabularyRepository vocabularyRepository;
    private final VocabularyMapper vocabularyMapper;
    public VocabularyService(VocabularyRepository vocabularyRepository, VocabularyMapper vocabularyMapper) {
        this.vocabularyRepository = vocabularyRepository;
        this.vocabularyMapper = vocabularyMapper;
    }

    public List<VocabularyDTORes> getByLessonId(Long lessonId) {
        // 1) Lấy entity đã JOIN FETCH mediaAssets
        var entities = vocabularyRepository.findByLessonVocabulary_IdAndIsForLearning(lessonId,true);

        // Debug entity
        entities.forEach(v ->
                System.out.println("[Entity] Vocab " + v.getId()
                        + " assets = " + (v.getMediaAssets() == null ? "null" : v.getMediaAssets().size()))
        );

        // 2) Map sang DTO
        var dtos = vocabularyMapper.toDtoList(entities);

        // Debug DTO
        dtos.forEach(d -> {
            var assets = d.getMediaAssets();
            System.out.println("[DTO] Vocab " + d.getId()
                    + " assets = " + (assets == null ? "null" : assets.size()));
            if (assets != null && !assets.isEmpty()) {
                var a = assets.get(0);
                System.out.println("[DTO] sample -> url=" + a.getUrl()
                        + ", mediaType=" + a.getMediaType()
                        + ", altText=" + a.getAltText());
            }
        });

        return dtos;
    }

    //chuc nang cho admin

}
