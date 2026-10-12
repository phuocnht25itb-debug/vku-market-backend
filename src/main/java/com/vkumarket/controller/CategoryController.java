package com.vkumarket.controller;

import com.vkumarket.dto.CategoryDto;
import com.vkumarket.repository.CategoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository categories;

    public CategoryController(CategoryRepository categories) {
        this.categories = categories;
    }

    @GetMapping
    public List<CategoryDto> all() {
        return categories.findAll().stream().map(c -> new CategoryDto(c.getId(), c.getName())).toList();
    }
}
