package com.quickbite.authuser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private Long id;
    private String label;
    private String streetAddress;
    private String city;
    private String district;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
}
