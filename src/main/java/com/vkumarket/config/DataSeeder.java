package com.vkumarket.config;

import com.vkumarket.entity.Category;
import com.vkumarket.entity.Role;
import com.vkumarket.entity.User;
import com.vkumarket.repository.CategoryRepository;
import com.vkumarket.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

/** Tạo dữ liệu mẫu lần chạy đầu: danh mục + tài khoản admin. */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(CategoryRepository categories, UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (categories.count() == 0) {
                categories.saveAll(List.of(
                        new Category("Giáo trình & sách"),
                        new Category("Đồ điện tử"),
                        new Category("Đồ dùng học tập"),
                        new Category("Đồ gia dụng & phòng trọ"),
                        new Category("Thời trang"),
                        new Category("Xe đạp & xe máy"),
                        new Category("Khác")));
            }
            if (!users.existsByEmail("admin@vku.udn.vn")) {
                User admin = new User();
                admin.setFullName("Quản trị viên");
                admin.setEmail("admin@vku.udn.vn");
                admin.setPasswordHash(encoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                users.save(admin);
            }
        };
    }
}
