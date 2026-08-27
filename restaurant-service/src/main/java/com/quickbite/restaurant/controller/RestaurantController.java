package com.quickbite.restaurant.controller;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.security.SecurityConstants;
import com.quickbite.restaurant.dto.CategoryDto;
import com.quickbite.restaurant.dto.RestaurantRequestDto;
import com.quickbite.restaurant.dto.RestaurantResponseDto;
import com.quickbite.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Endpoints for restaurant catalog and management")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @Operation(summary = "Register/create a new restaurant (Owner)")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> createRestaurant(
            @RequestHeader(value = SecurityConstants.USER_ID_HEADER, required = false, defaultValue = "1") Long ownerId,
            @Valid @RequestBody RestaurantRequestDto request) {
        RestaurantResponseDto response = restaurantService.createRestaurant(ownerId, request);
        return new ResponseEntity<>(ApiResponse.success("Restaurant registered successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant details by ID")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> getRestaurantById(@PathVariable Long id) {
        RestaurantResponseDto response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all active/open restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantResponseDto>>> getAllOpenRestaurants() {
        List<RestaurantResponseDto> restaurants = restaurantService.getAllOpenRestaurants();
        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    @GetMapping("/search")
    @Operation(summary = "Search restaurants by keyword")
    public ResponseEntity<ApiResponse<List<RestaurantResponseDto>>> searchRestaurants(@RequestParam String query) {
        List<RestaurantResponseDto> results = restaurantService.searchRestaurants(query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get restaurants by owner ID")
    public ResponseEntity<ApiResponse<List<RestaurantResponseDto>>> getRestaurantsByOwner(@PathVariable Long ownerId) {
        List<RestaurantResponseDto> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
        return ResponseEntity.ok(ApiResponse.success(restaurants));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update restaurant information")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>> updateRestaurant(
            @RequestHeader(value = SecurityConstants.USER_ID_HEADER, required = false, defaultValue = "1") Long ownerId,
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequestDto request) {
        RestaurantResponseDto response = restaurantService.updateRestaurant(ownerId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Restaurant updated successfully", response));
    }

    @PostMapping("/{id}/categories")
    @Operation(summary = "Add menu category for a restaurant")
    public ResponseEntity<ApiResponse<CategoryDto>> addCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto dto) {
        CategoryDto response = restaurantService.addCategory(id, dto);
        return new ResponseEntity<>(ApiResponse.success("Category added successfully", response), HttpStatus.CREATED);
    }
}
