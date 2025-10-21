package com.iuh.WiseOwlEnglish_Backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadImage(MultipartFile file, String folder);

    void deleteByPublicId(String publicId);
}
