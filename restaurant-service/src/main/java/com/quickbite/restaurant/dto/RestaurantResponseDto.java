package com.quickbite.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponseDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private String address;
    private String city;
    private String phoneNumber;
    private String imageUrl;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private boolean isOpen;
    private Double rating;
    private boolean isFeatured;
    private List<CategoryDto> categories;
    private List<MenuItemResponseDto> menuItems;
    private LocalDateTime createdAt;
}
