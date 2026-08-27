package com.quickbite.notification.controller;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.security.SecurityConstants;
import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for viewing notification feeds")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/me")
    @Operation(summary = "Get all notifications of the current user")
    public ResponseEntity<ApiResponse<List<Notification>>> getCurrentUserNotifications(
            @RequestHeader(value = SecurityConstants.USER_ID_HEADER, required = false, defaultValue = "1") Long userId) {
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
}
