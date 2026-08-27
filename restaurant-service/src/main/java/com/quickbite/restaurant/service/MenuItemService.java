package com.quickbite.restaurant.service;

import com.quickbite.common.dto.MenuItemSummaryDto;
import com.quickbite.common.exception.ResourceNotFoundException;
import com.quickbite.restaurant.dto.MenuItemRequestDto;
import com.quickbite.restaurant.dto.MenuItemResponseDto;
import com.quickbite.restaurant.entity.Category;
import com.quickbite.restaurant.entity.MenuItem;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.repository.CategoryRepository;
import com.quickbite.restaurant.repository.MenuItemRepository;
import com.quickbite.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public MenuItemResponseDto createMenuItem(MenuItemRequestDto request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", request.getRestaurantId()));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .available(request.isAvailable())
                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                .build();

        MenuItem saved = menuItemRepository.save(menuItem);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public MenuItemResponseDto getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        return mapToDto(menuItem);
    }

    @Transactional(readOnly = true)
    public MenuItemSummaryDto getMenuItemSummary(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        return MenuItemSummaryDto.builder()
                .id(menuItem.getId())
                .restaurantId(menuItem.getRestaurant().getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .imageUrl(menuItem.getImageUrl())
                .isAvailable(menuItem.isAvailable())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDto> getMenuItemsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponseDto updateMenuItem(Long id, MenuItemRequestDto request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            menuItem.setCategory(category);
        }

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setAvailable(request.isAvailable());
        menuItem.setPreparationTimeMinutes(request.getPreparationTimeMinutes());

        MenuItem updated = menuItemRepository.save(menuItem);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("MenuItem", "id", id);
        }
        menuItemRepository.deleteById(id);
    }

    private MenuItemResponseDto mapToDto(MenuItem m) {
        return MenuItemResponseDto.builder()
                .id(m.getId())
                .restaurantId(m.getRestaurant().getId())
                .categoryId(m.getCategory() != null ? m.getCategory().getId() : null)
                .categoryName(m.getCategory() != null ? m.getCategory().getName() : null)
                .name(m.getName())
                .description(m.getDescription())
                .price(m.getPrice())
                .imageUrl(m.getImageUrl())
                .available(m.isAvailable())
                .preparationTimeMinutes(m.getPreparationTimeMinutes())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
