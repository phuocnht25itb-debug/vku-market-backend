package com.vkumarket.dto;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int totalPages, long totalItems) {
}
