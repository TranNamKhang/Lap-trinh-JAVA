package com.homestay.repositories;

import com.homestay.models.Homestay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {
    List<Homestay> findByOwnerId(Long ownerId);

    List<Homestay> findByPricePerNightBetween(Double minPrice, Double maxPrice);

    List<Homestay> findByLocationContainingIgnoreCase(String location);

    List<Homestay> findByProvinceContainingIgnoreCase(String province);

    List<Homestay> findByNameContainingIgnoreCase(String name);
}
