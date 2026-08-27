package com.quickbite.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Long id;
    private Long restaurantId;
    @NotBlank(message = "Category name is required")
    private String name;
    private String description;
    private Integer displayOrder;
}
