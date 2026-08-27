package com.quickbite.restaurant.service;

import com.quickbite.common.exception.ResourceNotFoundException;
import com.quickbite.restaurant.dto.CategoryDto;
import com.quickbite.restaurant.dto.MenuItemResponseDto;
import com.quickbite.restaurant.dto.RestaurantRequestDto;
import com.quickbite.restaurant.dto.RestaurantResponseDto;
import com.quickbite.restaurant.entity.Category;
import com.quickbite.restaurant.entity.MenuItem;
import com.quickbite.restaurant.entity.Restaurant;
import com.quickbite.restaurant.repository.CategoryRepository;
import com.quickbite.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public RestaurantResponseDto createRestaurant(Long ownerId, RestaurantRequestDto request) {
        Restaurant restaurant = Restaurant.builder()
                .ownerId(ownerId)
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .phoneNumber(request.getPhoneNumber())
                .imageUrl(request.getImageUrl())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .isOpen(request.isOpen())
                .isFeatured(request.isFeatured())
                .rating(5.0)
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public RestaurantResponseDto getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));
        return mapToDto(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponseDto> getAllOpenRestaurants() {
        return restaurantRepository.findByIsOpenTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponseDto> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponseDto> searchRestaurants(String query) {
        return restaurantRepository.searchByNameOrDescription(query).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantResponseDto updateRestaurant(Long ownerId, Long id, RestaurantRequestDto request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", id));

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setPhoneNumber(request.getPhoneNumber());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setOpen(request.isOpen());
        restaurant.setFeatured(request.isFeatured());

        Restaurant updated = restaurantRepository.save(restaurant);
        return mapToDto(updated);
    }

    @Transactional
    public CategoryDto addCategory(Long restaurantId, CategoryDto dto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "id", restaurantId));

        Category category = Category.builder()
                .restaurant(restaurant)
                .name(dto.getName())
                .description(dto.getDescription())
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryDto.builder()
                .id(saved.getId())
                .restaurantId(restaurantId)
                .name(saved.getName())
                .description(saved.getDescription())
                .displayOrder(saved.getDisplayOrder())
                .build();
    }

    private RestaurantResponseDto mapToDto(Restaurant r) {
        List<CategoryDto> categories = r.getCategories() != null
                ? r.getCategories().stream().map(c -> CategoryDto.builder()
                .id(c.getId())
                .restaurantId(r.getId())
                .name(c.getName())
                .description(c.getDescription())
                .displayOrder(c.getDisplayOrder())
                .build()).collect(Collectors.toList())
                : List.of();

        List<MenuItemResponseDto> menuItems = r.getMenuItems() != null
                ? r.getMenuItems().stream().map(this::mapMenuItemToDto).collect(Collectors.toList())
                : List.of();

        return RestaurantResponseDto.builder()
                .id(r.getId())
                .ownerId(r.getOwnerId())
                .name(r.getName())
                .description(r.getDescription())
                .address(r.getAddress())
                .city(r.getCity())
                .phoneNumber(r.getPhoneNumber())
                .imageUrl(r.getImageUrl())
                .openingTime(r.getOpeningTime())
                .closingTime(r.getClosingTime())
                .isOpen(r.isOpen())
                .rating(r.getRating())
                .isFeatured(r.isFeatured())
                .categories(categories)
                .menuItems(menuItems)
                .createdAt(r.getCreatedAt())
                .build();
    }

    private MenuItemResponseDto mapMenuItemToDto(MenuItem m) {
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
