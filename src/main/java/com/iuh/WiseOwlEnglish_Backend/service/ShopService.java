package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.ShopDataRes;
import com.iuh.WiseOwlEnglish_Backend.exception.BadRequestException;
import com.iuh.WiseOwlEnglish_Backend.exception.NotFoundException;
import com.iuh.WiseOwlEnglish_Backend.model.CategorySticker;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerProfile;
import com.iuh.WiseOwlEnglish_Backend.model.LearnerSticker;
import com.iuh.WiseOwlEnglish_Backend.model.Sticker;
import com.iuh.WiseOwlEnglish_Backend.repository.CategoryStickerRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerProfileRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerStickerRepository;
import com.iuh.WiseOwlEnglish_Backend.repository.StickerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {
    private final StickerRepository stickerRepo;
    private final LearnerStickerRepository learnerStickerRepo;
    private final LearnerProfileRepository learnerProfileRepo;
    private final CategoryStickerRepository categoryStickerRepo;

    @Transactional(readOnly = true)
    public ShopDataRes getShopData(Long learnerId) {
        LearnerProfile profile = learnerProfileRepo.findById(learnerId)
                .orElseThrow(() -> new NotFoundException("Learner not found"));

        List<LearnerSticker> owned = learnerStickerRepo.findByLearnerProfile_Id(learnerId);
        List<Long> ownedIds = owned.stream().map(ls -> ls.getSticker().getId()).toList();

        // Lấy tất cả category và sticker bên trong
        List<CategorySticker> categories = categoryStickerRepo.findAll();

        ShopDataRes res = new ShopDataRes();
        res.setCurrentBalance(profile.getPointBalance());
        res.setOwnedStickerIds(ownedIds);

        // Map Entity sang DTO phân nhóm
        List<ShopDataRes.CategoryGroupDto> catDtos = categories.stream().map(cat -> {
            ShopDataRes.CategoryGroupDto cDto = new ShopDataRes.CategoryGroupDto();
            cDto.setId(cat.getId());
            cDto.setName(cat.getCategoryName());

            List<ShopDataRes.StickerItemDto> sDtos = cat.getStickers().stream().map(s -> {
                ShopDataRes.StickerItemDto sDto = new ShopDataRes.StickerItemDto();
                sDto.setId(s.getId());
                sDto.setName(s.getName());
                sDto.setImageUrl(s.getImageUrl());
                sDto.setPrice(s.getPrice());
                sDto.setRarity(s.getRarity().name());
                return sDto;
            }).toList();

            cDto.setStickers(sDtos);
            return cDto;
        }).toList();

        res.setCategories(catDtos);
        return res;
    }

    @Transactional
    public void buySticker(Long learnerId, Long stickerId) {
        LearnerProfile profile = learnerProfileRepo.findById(learnerId)
                .orElseThrow(() -> new NotFoundException("Learner not found"));
        Sticker sticker = stickerRepo.findById(stickerId)
                .orElseThrow(() -> new NotFoundException("Sticker not found"));

        if (learnerStickerRepo.existsByLearnerProfile_IdAndSticker_Id(learnerId, stickerId)) {
            throw new BadRequestException("Bạn đã sở hữu sticker này rồi!");
        }

        if (profile.getPointBalance() < sticker.getPrice()) {
            throw new BadRequestException("Không đủ điểm thưởng (" + profile.getPointBalance() + "/" + sticker.getPrice() + ")");
        }

        // Trừ tiền
        profile.setPointBalance(profile.getPointBalance() - sticker.getPrice());
        learnerProfileRepo.save(profile);

        // Lưu sở hữu
        LearnerSticker ls = new LearnerSticker();
        ls.setLearnerProfile(profile);
        ls.setSticker(sticker);
        ls.setPurchasedAt(LocalDateTime.now());
        learnerStickerRepo.save(ls);
    }

    @Transactional
    public void equipSticker(Long learnerId, Long stickerId) {
        LearnerProfile profile = learnerProfileRepo.findById(learnerId)
                .orElseThrow(() -> new NotFoundException("Learner not found"));

        boolean owned = learnerStickerRepo.existsByLearnerProfile_IdAndSticker_Id(learnerId, stickerId);
        if (!owned) {
            throw new BadRequestException("Bạn chưa sở hữu sticker này!");
        }

        Sticker sticker = stickerRepo.findById(stickerId)
                .orElseThrow(() -> new NotFoundException("Sticker not found"));

        profile.setAvatarUrl(sticker.getImageUrl());
        learnerProfileRepo.save(profile);
    }
}
