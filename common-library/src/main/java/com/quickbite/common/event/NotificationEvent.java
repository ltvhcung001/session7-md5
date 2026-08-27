package com.quickbite.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String eventId;
    private Long recipientId;
    private String recipientEmail;
    private String recipientPhone;
    private String title;
    private String message;
    private String channel; // EMAIL, SMS, PUSH, WEBSOCKET
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
