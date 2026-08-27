package com.quickbite.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequestDto {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    private String city;

    private String phoneNumber;

    private String imageUrl;

    private LocalTime openingTime;

    private LocalTime closingTime;

    @Builder.Default
    private boolean isOpen = true;

    @Builder.Default
    private boolean isFeatured = false;
}
