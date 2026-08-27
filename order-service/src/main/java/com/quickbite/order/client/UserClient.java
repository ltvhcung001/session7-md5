package com.quickbite.order.client;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.dto.UserSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-user-service")
public interface UserClient {

    @GetMapping("/api/v1/users/{id}/summary")
    ApiResponse<UserSummaryDto> getUserSummary(@PathVariable("id") Long id);
}
