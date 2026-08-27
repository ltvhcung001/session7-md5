package com.quickbite.restaurant.controller;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.dto.MenuItemSummaryDto;
import com.quickbite.restaurant.dto.MenuItemRequestDto;
import com.quickbite.restaurant.dto.MenuItemResponseDto;
import com.quickbite.restaurant.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Tag(name = "Menu Items", description = "Endpoints for managing food items and menus")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @Operation(summary = "Add a new menu item to a restaurant")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> createMenuItem(@Valid @RequestBody MenuItemRequestDto request) {
        MenuItemResponseDto response = menuItemService.createMenuItem(request);
        return new ResponseEntity<>(ApiResponse.success("Menu item created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get menu item by ID")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> getMenuItemById(@PathVariable Long id) {
        MenuItemResponseDto response = menuItemService.getMenuItemById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Internal inter-service call: Get menu item summary")
    public ResponseEntity<ApiResponse<MenuItemSummaryDto>> getMenuItemSummary(@PathVariable Long id) {
        MenuItemSummaryDto response = menuItemService.getMenuItemSummary(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get all available menu items of a restaurant")
    public ResponseEntity<ApiResponse<List<MenuItemResponseDto>>> getMenuItemsByRestaurant(@PathVariable Long restaurantId) {
        List<MenuItemResponseDto> items = menuItemService.getMenuItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a menu item")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequestDto request) {
        MenuItemResponseDto response = menuItemService.updateMenuItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully", null));
    }
}
