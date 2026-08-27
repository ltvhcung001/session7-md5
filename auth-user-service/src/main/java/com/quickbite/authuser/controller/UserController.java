package com.quickbite.authuser.controller;

import com.quickbite.authuser.dto.AddressDto;
import com.quickbite.authuser.dto.UpdateProfileRequest;
import com.quickbite.authuser.dto.UserResponseDto;
import com.quickbite.authuser.service.UserService;
import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.dto.UserSummaryDto;
import com.quickbite.common.security.SecurityConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for managing user profiles and addresses")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) Long userId) {
        UserResponseDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponseDto profile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Internal inter-service call: Get user summary by ID")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getUserSummary(@PathVariable("id") Long id) {
        UserSummaryDto summary = userService.getUserSummary(id);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add delivery address for current user")
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody AddressDto addressDto) {
        AddressDto address = userService.addAddress(userId, addressDto);
        return new ResponseEntity<>(ApiResponse.success("Address added successfully", address), HttpStatus.CREATED);
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "Get all addresses of current user")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getUserAddresses(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) Long userId) {
        List<AddressDto> addresses = userService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @RequestHeader(SecurityConstants.USER_ID_HEADER) Long userId,
            @PathVariable Long addressId) {
        userService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }
}
