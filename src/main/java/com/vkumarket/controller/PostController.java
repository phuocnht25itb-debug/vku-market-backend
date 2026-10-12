package com.vkumarket.controller;

import com.vkumarket.dto.PageResponse;
import com.vkumarket.dto.PostDto;
import com.vkumarket.dto.PostRequest;
import com.vkumarket.entity.ItemCondition;
import com.vkumarket.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PostController {
    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    private static String email(Authentication a) {
        return (a == null || a instanceof AnonymousAuthenticationToken) ? null : a.getName();
    }

    @GetMapping("/posts")
    public PageResponse<PostDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return service.search(q, categoryId, minPrice, maxPrice, condition, sort, page, size);
    }

    @GetMapping("/posts/{id}")
    public PostDto detail(@PathVariable Long id, Authentication auth) {
        return service.getOne(id, email(auth));
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostDto create(@Valid @ModelAttribute PostRequest req,
                          @RequestParam(value = "images", required = false) List<MultipartFile> images,
                          Authentication auth) {
        return service.create(auth.getName(), req, images);
    }

    @PutMapping("/posts/{id}")
    public PostDto update(@PathVariable Long id, @Valid @RequestBody PostRequest req, Authentication auth) {
        return service.update(auth.getName(), id, req);
    }

    @DeleteMapping("/posts/{id}")
    public Map<String, String> delete(@PathVariable Long id, Authentication auth) {
        service.delete(auth.getName(), id);
        return Map.of("message", "Đã xóa tin");
    }

    @PatchMapping("/posts/{id}/sold")
    public PostDto sold(@PathVariable Long id, Authentication auth) {
        return service.markSold(auth.getName(), id);
    }

    @GetMapping("/my/posts")
    public List<PostDto> mine(Authentication auth) {
        return service.myPosts(auth.getName());
    }
}
