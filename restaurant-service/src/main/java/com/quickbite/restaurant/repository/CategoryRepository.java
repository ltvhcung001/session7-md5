package com.quickbite.restaurant.repository;

import com.quickbite.restaurant.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByRestaurantIdOrderByDisplayOrderAsc(Long restaurantId);
}
