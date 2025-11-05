package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.request.CreateLearnerProfileReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.LearnerProfileReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LearnerProfileRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.ProfileRes;
import com.iuh.WiseOwlEnglish_Backend.enums.ProgressStatus;
import com.iuh.WiseOwlEnglish_Backend.exception.*;
import com.iuh.WiseOwlEnglish_Backend.mapper.LearnerProfileMapper;
import com.iuh.WiseOwlEnglish_Backend.model.GradeLevel;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerGradeProgress;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerProfile;
import com.iuh.WiseOwlEnglish_Backend.model.UserAccount;
import com.iuh.WiseOwlEnglish_Backend.repository.GradeLevelRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerGradeProgressRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerProfileRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LearnerProfileService {
    private final LearnerProfileRepository learnerProfileRepo;
    private final UserAccountRepository userAccountRepo;
    private final LearnerProfileMapper learnerProfileMapper;

    private final GradeLevelRepository gradeRepo;
    private final LearnerGradeProgressRepository progressRepo;



    @Transactional
    public LearnerProfileRes createForCurrentUserByEmail(String email, LearnerProfileReq req) {
        UserAccount user = userAccountRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found by email: " + email));

        // Tự set thời gian (không dùng auditing)
        LocalDateTime now = LocalDateTime.now();

        LearnerProfile p = new LearnerProfile();
        p.setFullName(req.getFullName());
        p.setNickName(req.getNickName());
        p.setDateOfBirth(req.getDateOfBirth());
        p.setUserAccount(user);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        p.setAvatarUrl(req.getAvatarUrl());

        p = learnerProfileRepo.save(p);

        LearnerProfileRes res = new LearnerProfileRes();
        res.setId(p.getId());
        res.setFullName(p.getFullName());
        res.setNickName(p.getNickName());
        res.setDateOfBirth(p.getDateOfBirth());
        res.setUserId(user.getId());
        res.setCreatedAt(p.getCreatedAt());
        res.setUpdatedAt(p.getUpdatedAt());
        res.setAvatarUrl(p.getAvatarUrl());

        return res;
    }

    public List<LearnerProfileRes> getLearnerProfilesByUserId(Long id) {
        List<LearnerProfile> profiles = learnerProfileRepo.findByUserAccount_Id(id);
        List<LearnerProfileRes> resList = learnerProfileMapper.toDTOs(profiles);
        return resList;
    }




    @Transactional
    public ProfileRes createProfile(CreateLearnerProfileReq req, Long userId) {
        System.out.println("CHECK REQUEST:"+ req.getFullName() + ", " + req.getInitialGradeLevelId());
        // 1) Tìm UserAccount hiện tại (nếu gắn profile theo user)
        String userIdStr = String.valueOf(userId);
        UserAccount owner = userAccountRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(userIdStr+" user nay không tồn tại"));

        // 2) Lưu LearnerProfile
        var now = LocalDateTime.now();
        LearnerProfile lp = new LearnerProfile();
        lp.setFullName(req.getFullName());
        lp.setNickName(req.getNickName());
        lp.setDateOfBirth(req.getDateOfBirth());
        lp.setAvatarUrl(req.getAvatarUrl());
        lp.setCreatedAt(now);
        lp.setUpdatedAt(now);
        lp.setUserAccount(owner);
        lp = learnerProfileRepo.save(lp); // có id

        // 3) Lấy danh sách 5 GradeLevels theo thứ tự
        List<GradeLevel> grades = gradeRepo.findAllByOrderByOrderIndexAsc();
        if (grades.size() != 5) {
            throw new BusinessException(ErrorCode.GRADE_DATA_INVALID,"Cần có đúng 5 GradeLevel theo orderIndex 1..5");
        }

        // 4) Xác định "lớp khởi tạo"
        GradeLevel initialGrade = resolveInitialGrade(req, grades);

        // 5) Tạo 5 dòng progress
        List<LearnerGradeProgress> progresses = new ArrayList<>(5);
        for (GradeLevel g : grades) {
            LearnerGradeProgress p = new LearnerGradeProgress();
            p.setLearnerProfile(lp);
            p.setGradeLevel(g);

            boolean isInitial = g.getId().equals(initialGrade.getId());
            p.setStatus(isInitial ? ProgressStatus.IN_PROGRESS : ProgressStatus.LOCKED);
            p.setPrimary(isInitial? true : false);
            p.setStartedAt(isInitial ? now : null);
            p.setCompletedAt(null);

            progresses.add(p);
        }
        progressRepo.saveAll(progresses);

        // 6) Trả response
        return mapToRes(lp, progresses);
    }

    private GradeLevel resolveInitialGrade(CreateLearnerProfileReq req, List<GradeLevel> grades) {
        Long id = req.getInitialGradeLevelId();
        if (id == null) {
            throw new BadRequestException("Vui lòng truyền initialGradeLevelId");
        }
        return grades.stream()
                .filter(g -> Objects.equals(g.getId(), id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "GradeLevel id="+id+" không tồn tại hoặc không hợp lệ"));
    }


    private ProfileRes mapToRes(LearnerProfile lp, List<LearnerGradeProgress> progresses) {
        var res = new ProfileRes();
        res.setId(lp.getId());
        res.setFullName(lp.getFullName());
        res.setNickName(lp.getNickName());
        res.setAge(lp.getAge());
        res.setAvatarUrl(lp.getAvatarUrl());

        var items = progresses.stream()
                .sorted(Comparator.comparing(p -> p.getGradeLevel().getOrderIndex()))
                .map(p -> new ProfileRes.GradeProgressItem(
                        p.getGradeLevel().getId(),
                        p.getGradeLevel().getGradeName(),
                        p.getGradeLevel().getOrderIndex(),
                        p.getStatus().name(),
                        p.getStartedAt(),
                        p.getCompletedAt()
                ))
                .toList();
        res.setGradeProgresses(items);
        return res;
    }

    public LearnerProfileRes getLearnerProfile(Long profileId){
        LearnerProfile proflie = learnerProfileRepo.findById(profileId).orElseThrow(()-> new NotFoundException("Không tìm thấy profile: "+profileId));
        return learnerProfileMapper.toDTO(proflie);
    }

}
