package com.quickbite.authuser.service;

import com.quickbite.authuser.dto.AddressDto;
import com.quickbite.authuser.dto.UpdateProfileRequest;
import com.quickbite.authuser.dto.UserResponseDto;
import com.quickbite.authuser.entity.Address;
import com.quickbite.authuser.entity.User;
import com.quickbite.authuser.repository.AddressRepository;
import com.quickbite.authuser.repository.UserRepository;
import com.quickbite.common.dto.UserSummaryDto;
import com.quickbite.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public UserResponseDto getUserProfile(Long userId) {
        User user = getUserEntity(userId);
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public UserSummaryDto getUserSummary(Long userId) {
        User user = getUserEntity(userId);
        return UserSummaryDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public UserResponseDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserEntity(userId);
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        User updated = userRepository.save(user);
        return mapToDto(updated);
    }

    @Transactional
    public AddressDto addAddress(Long userId, AddressDto addressDto) {
        User user = getUserEntity(userId);
        
        if (addressDto.isDefault()) {
            // Unset previous defaults
            user.getAddresses().forEach(a -> a.setDefault(false));
        }

        Address address = Address.builder()
                .user(user)
                .label(addressDto.getLabel())
                .streetAddress(addressDto.getStreetAddress())
                .city(addressDto.getCity())
                .district(addressDto.getDistrict())
                .postalCode(addressDto.getPostalCode())
                .latitude(addressDto.getLatitude())
                .longitude(addressDto.getLongitude())
                .isDefault(addressDto.isDefault() || user.getAddresses().isEmpty())
                .build();

        Address saved = addressRepository.save(address);
        return mapAddressToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapAddressToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        if (!address.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Address not found for current user");
        }
        addressRepository.delete(address);
    }

    public User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserResponseDto mapToDto(User user) {
        List<AddressDto> addresses = user.getAddresses() != null
                ? user.getAddresses().stream().map(this::mapAddressToDto).collect(Collectors.toList())
                : List.of();

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .active(user.isActive())
                .addresses(addresses)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressDto mapAddressToDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .label(address.getLabel())
                .streetAddress(address.getStreetAddress())
                .city(address.getCity())
                .district(address.getDistrict())
                .postalCode(address.getPostalCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.isDefault())
                .build();
    }
}
