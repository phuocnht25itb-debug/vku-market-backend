package com.vkumarket.controller;

import com.vkumarket.dto.PostDto;
import com.vkumarket.service.PostService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final PostService service;

    public AdminController(PostService service) {
        this.service = service;
    }

    @GetMapping("/posts/pending")
    public List<PostDto> pending() {
        return service.pending();
    }

    @PostMapping("/posts/{id}/approve")
    public PostDto approve(@PathVariable Long id) {
        return service.review(id, true);
    }

    @PostMapping("/posts/{id}/reject")
    public PostDto reject(@PathVariable Long id) {
        return service.review(id, false);
    }
}
