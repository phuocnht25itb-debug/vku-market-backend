package com.vkumarket.service;

import com.vkumarket.dto.PageResponse;
import com.vkumarket.dto.PostDto;
import com.vkumarket.dto.PostRequest;
import com.vkumarket.entity.*;
import com.vkumarket.exception.ApiException;
import com.vkumarket.repository.CategoryRepository;
import com.vkumarket.repository.PostRepository;
import com.vkumarket.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {
    private static final int MAX_IMAGES = 5;

    private final PostRepository posts;
    private final CategoryRepository categories;
    private final UserRepository users;
    private final FileStorageService storage;

    public PostService(PostRepository posts, CategoryRepository categories,
                       UserRepository users, FileStorageService storage) {
        this.posts = posts;
        this.categories = categories;
        this.users = users;
        this.storage = storage;
    }

    // ---------- helpers ----------
    private User userByEmail(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập"));
    }

    private Post find(Long id) {
        return posts.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy tin đăng"));
    }

    private Post findOwned(Long id, String email) {
        Post p = find(id);
        if (!p.getSeller().getEmail().equals(email)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Bạn không có quyền với tin này");
        }
        return p;
    }

    // ---------- public ----------
    @Transactional(readOnly = true)
    public PageResponse<PostDto> search(String q, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                        ItemCondition condition, String sort, int page, int size) {
        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), PostStatus.APPROVED));
            if (q != null && !q.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("title")), "%" + q.trim().toLowerCase() + "%"));
            }
            if (categoryId != null) ps.add(cb.equal(root.get("category").get("id"), categoryId));
            if (minPrice != null) ps.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) ps.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            if (condition != null) ps.add(cb.equal(root.get("itemCondition"), condition));
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Sort order = switch (sort == null ? "" : sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Page<Post> result = posts.findAll(spec, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), order));
        return new PageResponse<>(result.getContent().stream().map(PostDto::from).toList(),
                result.getNumber(), result.getTotalPages(), result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PostDto getOne(Long id, String email) {
        Post p = find(id);
        if (p.getStatus() == PostStatus.APPROVED || p.getStatus() == PostStatus.SOLD) {
            return PostDto.from(p);
        }
        if (email != null) {
            User u = userByEmail(email);
            if (u.getRole() == Role.ADMIN || p.getSeller().getId().equals(u.getId())) {
                return PostDto.from(p);
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy tin đăng");
    }

    // ---------- người dùng ----------
    @Transactional
    public PostDto create(String email, PostRequest req, List<MultipartFile> files) {
        User seller = userByEmail(email);
        Category category = categories.findById(req.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Danh mục không tồn tại"));

        List<MultipartFile> images = files == null ? List.of()
                : files.stream().filter(f -> !f.isEmpty()).toList();
        if (images.size() > MAX_IMAGES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tối đa " + MAX_IMAGES + " ảnh");
        }

        Post p = new Post();
        p.setTitle(req.title().trim());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setItemCondition(req.itemCondition());
        p.setCategory(category);
        p.setSeller(seller);
        p.setStatus(PostStatus.PENDING);
        for (MultipartFile f : images) {
            p.getImages().add(new PostImage(p, storage.storeImage(f)));
        }
        return PostDto.from(posts.save(p));
    }

    @Transactional
    public PostDto update(String email, Long id, PostRequest req) {
        Post p = findOwned(id, email);
        if (p.getStatus() == PostStatus.SOLD) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tin đã bán, không thể sửa");
        }
        Category category = categories.findById(req.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Danh mục không tồn tại"));
        p.setTitle(req.title().trim());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setItemCondition(req.itemCondition());
        p.setCategory(category);
        p.setStatus(PostStatus.PENDING); // sửa xong phải được duyệt lại
        return PostDto.from(p);
    }

    @Transactional
    public void delete(String email, Long id) {
        Post p = find(id);
        User u = userByEmail(email);
        if (u.getRole() != Role.ADMIN && !p.getSeller().getId().equals(u.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa tin này");
        }
        posts.delete(p);
    }

    @Transactional
    public PostDto markSold(String email, Long id) {
        Post p = findOwned(id, email);
        if (p.getStatus() != PostStatus.APPROVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Chỉ tin đã được duyệt mới đánh dấu đã bán");
        }
        p.setStatus(PostStatus.SOLD);
        return PostDto.from(p);
    }

    @Transactional(readOnly = true)
    public List<PostDto> myPosts(String email) {
        User u = userByEmail(email);
        return posts.findBySellerIdOrderByCreatedAtDesc(u.getId()).stream().map(PostDto::from).toList();
    }

    // ---------- admin ----------
    @Transactional(readOnly = true)
    public List<PostDto> pending() {
        return posts.findByStatusOrderByCreatedAtAsc(PostStatus.PENDING).stream().map(PostDto::from).toList();
    }

    @Transactional
    public PostDto review(Long id, boolean approve) {
        Post p = find(id);
        if (p.getStatus() != PostStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tin này không ở trạng thái chờ duyệt");
        }
        p.setStatus(approve ? PostStatus.APPROVED : PostStatus.REJECTED);
        return PostDto.from(p);
    }
}
