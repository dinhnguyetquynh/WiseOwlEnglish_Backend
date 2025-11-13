package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.LessonProgress;
import com.iuh.WiseOwlEnglish_Backend.repository.GameAttemptRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonProgressRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LessonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GradeProgressService {
    private final LessonProgressRepository lessonProgressRepo;
    private final LessonRepository lessonRepository;
    private final GameAttemptRepository attemptRepository;//total reward count


}
