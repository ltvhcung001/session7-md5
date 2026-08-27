package com.quickbite.authuser.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private String label; // e.g., HOME, WORK, OTHER

    @Column(nullable = false)
    private String streetAddress;

    private String city;

    private String district;

    private String postalCode;

    private Double latitude;

    private Double longitude;

    @Builder.Default
    private boolean isDefault = false;
}
