package com.homestay.repositories;

import com.homestay.models.Homestay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {
    List<Homestay> findByLocationContaining(String location);
    List<Homestay> findByOwnerId(Long ownerId);
}
