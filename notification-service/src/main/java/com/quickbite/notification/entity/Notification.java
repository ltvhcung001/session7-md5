package com.quickbite.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientId;

    private String recipientEmail;

    private String recipientPhone;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 30)
    private String channel; // EMAIL, SMS, PUSH, IN_APP

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "SENT";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
