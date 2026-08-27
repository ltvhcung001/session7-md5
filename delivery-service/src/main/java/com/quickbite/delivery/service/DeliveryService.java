package com.quickbite.delivery.service;

import com.quickbite.common.enums.DeliveryStatus;
import com.quickbite.common.event.DeliveryStatusUpdatedEvent;
import com.quickbite.common.event.OrderPreparedEvent;
import com.quickbite.common.exception.ResourceNotFoundException;
import com.quickbite.delivery.dto.DeliveryResponseDto;
import com.quickbite.delivery.dto.UpdateDeliveryStatusRequest;
import com.quickbite.delivery.entity.Delivery;
import com.quickbite.delivery.kafka.DeliveryEventProducer;
import com.quickbite.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventProducer deliveryEventProducer;

    @Transactional
    public void handleOrderPreparedEvent(OrderPreparedEvent event) {
        log.info("Handling OrderPreparedEvent for orderId: {}", event.getOrderId());

        // Automatically assign a simulated nearby driver
        Long simulatedDriverId = 101L;
        String simulatedDriverName = "Alex Nguyen (Driver)";
        String simulatedDriverPhone = "+84 987 654 321";

        Delivery delivery = Delivery.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .restaurantId(event.getRestaurantId())
                .pickupAddress(event.getRestaurantAddress())
                .deliveryAddress(event.getDeliveryAddress())
                .driverId(simulatedDriverId)
                .driverName(simulatedDriverName)
                .driverPhone(simulatedDriverPhone)
                .status(DeliveryStatus.DRIVER_ASSIGNED)
                .estimatedMinutes(25)
                .notes("Driver assigned and heading to restaurant for pickup")
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        // Publish event
        publishStatusEvent(saved);
    }

    @Transactional
    public DeliveryResponseDto updateDeliveryStatus(Long deliveryId, UpdateDeliveryStatusRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "id", deliveryId));

        delivery.setStatus(request.getStatus());
        if (request.getCurrentLatitude() != null) delivery.setCurrentLatitude(request.getCurrentLatitude());
        if (request.getCurrentLongitude() != null) delivery.setCurrentLongitude(request.getCurrentLongitude());
        if (request.getNotes() != null) delivery.setNotes(request.getNotes());

        Delivery updated = deliveryRepository.save(delivery);

        publishStatusEvent(updated);

        return mapToDto(updated);
    }

    @Transactional(readOnly = true)
    public DeliveryResponseDto getDeliveryByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", "orderId", orderId));
        return mapToDto(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponseDto> getDeliveriesByDriver(Long driverId) {
        return deliveryRepository.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void publishStatusEvent(Delivery delivery) {
        DeliveryStatusUpdatedEvent event = DeliveryStatusUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .deliveryId(delivery.getId())
                .orderId(delivery.getOrderId())
                .driverId(delivery.getDriverId())
                .driverName(delivery.getDriverName())
                .driverPhone(delivery.getDriverPhone())
                .customerId(delivery.getCustomerId())
                .status(delivery.getStatus())
                .notes(delivery.getNotes())
                .timestamp(LocalDateTime.now())
                .build();

        deliveryEventProducer.sendDeliveryStatusUpdatedEvent(event);
    }

    private DeliveryResponseDto mapToDto(Delivery d) {
        return DeliveryResponseDto.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .driverId(d.getDriverId())
                .driverName(d.getDriverName())
                .driverPhone(d.getDriverPhone())
                .customerId(d.getCustomerId())
                .restaurantId(d.getRestaurantId())
                .pickupAddress(d.getPickupAddress())
                .deliveryAddress(d.getDeliveryAddress())
                .status(d.getStatus())
                .currentLatitude(d.getCurrentLatitude())
                .currentLongitude(d.getCurrentLongitude())
                .estimatedMinutes(d.getEstimatedMinutes())
                .notes(d.getNotes())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
