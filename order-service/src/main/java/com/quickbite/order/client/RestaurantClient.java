package com.quickbite.order.client;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.dto.MenuItemSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {

    @GetMapping("/api/v1/restaurants/{id}")
    ApiResponse<Map<String, Object>> getRestaurantById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/menus/{id}/summary")
    ApiResponse<MenuItemSummaryDto> getMenuItemSummary(@PathVariable("id") Long id);
}
