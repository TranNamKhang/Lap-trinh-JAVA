package com.homestay.repositories;

import com.homestay.models.Homestay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {
    List<Homestay> findByOwnerId(Long ownerId);

    List<Homestay> findByPricePerNightBetween(Double minPrice, Double maxPrice);

    // Tìm homestay theo địa điểm
    List<Homestay> findByLocationContainingIgnoreCase(String location);

    // Tìm homestay theo tên
    List<Homestay> findByNameContainingIgnoreCase(String name);

    List<Homestay> findByCategoryId(Long categoryId);

    long countByCategoryId(Long id);
}
