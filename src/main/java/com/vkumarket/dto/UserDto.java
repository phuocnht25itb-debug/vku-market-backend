package com.vkumarket.dto;

import com.vkumarket.entity.User;

public record UserDto(Long id, String fullName, String email, String phone, String role) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole().name());
    }
}
