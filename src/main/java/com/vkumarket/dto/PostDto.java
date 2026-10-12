package com.vkumarket.dto;

import com.vkumarket.entity.Post;
import com.vkumarket.entity.PostImage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PostDto(
        Long id, String title, String description, BigDecimal price,
        String itemCondition, String status,
        Long categoryId, String categoryName,
        Long sellerId, String sellerName, String sellerPhone, String sellerEmail,
        List<String> images, LocalDateTime createdAt) {

    public static PostDto from(Post p) {
        return new PostDto(
                p.getId(), p.getTitle(), p.getDescription(), p.getPrice(),
                p.getItemCondition().name(), p.getStatus().name(),
                p.getCategory().getId(), p.getCategory().getName(),
                p.getSeller().getId(), p.getSeller().getFullName(),
                p.getSeller().getPhone(), p.getSeller().getEmail(),
                p.getImages().stream().map(PostImage::getUrl).toList(),
                p.getCreatedAt());
    }
}
