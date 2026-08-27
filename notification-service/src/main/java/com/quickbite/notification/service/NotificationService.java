package com.quickbite.notification.service;

import com.quickbite.notification.entity.Notification;
import com.quickbite.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification sendNotification(Long recipientId, String email, String phone, String title, String message, String channel) {
        log.info("[NOTIFICATION DISPATCHED] -> Channel: {}, To: (ID: {}, Email: {}, Phone: {}), Title: '{}', Content: '{}'",
                channel, recipientId, email, phone, title, message);

        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .recipientEmail(email)
                .recipientPhone(phone)
                .title(title)
                .message(message)
                .channel(channel)
                .status("SENT")
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }
}
