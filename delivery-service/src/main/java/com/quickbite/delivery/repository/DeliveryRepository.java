package com.quickbite.delivery.repository;

import com.quickbite.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
    List<Delivery> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    List<Delivery> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
