package com.iuh.WiseOwlEnglish_Backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService implements StorageService{
    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String defaultFolder;

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5MB
    @Override
    public String uploadImage(MultipartFile file, String folder) {
        validate(file);

        String targetFolder = (folder == null || folder.isBlank()) ? defaultFolder : folder;

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", targetFolder);
            options.put("resource_type", "image");
            options.put("use_filename", true);
            options.put("unique_filename", true);
            options.put("overwrite", false);
            // Tối ưu: auto format + auto quality (giảm băng thông, vẫn đẹp)
            options.put("transformation",
                    new Transformation<>()
                            .fetchFormat("auto")  // f_auto
                            .quality("auto")      // q_auto
                    // Nếu là avatar, có thể crop center (tuỳ chọn):
                    //.gravity("auto").crop("fill").width(512).height(512)
            );

            Map<?, ?> uploadRes = cloudinary.uploader().upload(file.getBytes(), options);

            // Lấy ra secure_url (https), public_id, width/height… nếu cần
            String secureUrl = (String) uploadRes.get("secure_url");
            // String publicId = (String) uploadRes.get("public_id");
            return secureUrl;
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteByPublicId(String publicId) {

    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File rỗng.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Ảnh quá lớn (>5MB).");
        }
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.startsWith("image/") ||
                        contentType.equals(MediaType.IMAGE_JPEG_VALUE) ||
                        contentType.equals(MediaType.IMAGE_PNG_VALUE) ||
                        contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("Chỉ hỗ trợ file ảnh (jpg/png/webp).");
        }
    }
}
