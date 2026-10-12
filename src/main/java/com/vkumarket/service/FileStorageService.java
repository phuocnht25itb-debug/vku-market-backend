package com.vkumarket.service;

import com.vkumarket.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        this.root = Path.of(uploadDir).toAbsolutePath();
        Files.createDirectories(root);
    }

    /** Lưu ảnh, trả về đường dẫn công khai dạng /uploads/xxx.jpg */
    public String storeImage(MultipartFile file) {
        String type = file.getContentType();
        String ext;
        if (type == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "File không hợp lệ");
        }
        switch (type) {
            case "image/jpeg" -> ext = ".jpg";
            case "image/png" -> ext = ".png";
            case "image/webp" -> ext = ".webp";
            case "image/gif" -> ext = ".gif";
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận ảnh JPG, PNG, WEBP, GIF");
        }
        String name = UUID.randomUUID() + ext;
        try {
            Files.copy(file.getInputStream(), root.resolve(name));
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Không lưu được ảnh");
        }
        return "/uploads/" + name;
    }
}
