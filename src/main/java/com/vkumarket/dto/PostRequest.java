package com.vkumarket.dto;

import com.vkumarket.entity.ItemCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PostRequest(
        @NotBlank(message = "Vui lòng nhập tiêu đề") @Size(max = 150, message = "Tiêu đề tối đa 150 ký tự") String title,
        @Size(max = 4000, message = "Mô tả quá dài") String description,
        @NotNull(message = "Vui lòng nhập giá") @DecimalMin(value = "0", message = "Giá không hợp lệ") BigDecimal price,
        @NotNull(message = "Vui lòng chọn tình trạng") ItemCondition itemCondition,
        @NotNull(message = "Vui lòng chọn danh mục") Long categoryId) {
}
