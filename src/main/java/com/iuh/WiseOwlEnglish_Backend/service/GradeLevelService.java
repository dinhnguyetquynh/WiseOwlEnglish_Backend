package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.GradeLevelDTO;
import com.iuh.WiseOwlEnglish_Backend.mapper.GradeLevelMapper;
import com.iuh.WiseOwlEnglish_Backend.model.GradeLevel;
import com.iuh.WiseOwlEnglish_Backend.repository.GradeLevelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeLevelService {
    private final GradeLevelRepository gradeLevelRepository;
    private final GradeLevelMapper gradeLevelMapper;
    public GradeLevelService(GradeLevelRepository gradeLevelRepository, GradeLevelMapper gradeLevelMapper) {
        this.gradeLevelRepository = gradeLevelRepository;
        this.gradeLevelMapper = gradeLevelMapper;
    }

    public List<GradeLevelDTO> getAllGradeLevels() {
        return gradeLevelRepository.findAll()
                .stream()
                .map(gradeLevelMapper::toDTO)
                .toList();
    }


}
