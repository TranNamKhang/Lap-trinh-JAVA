package com.homestay.repositories;

import com.homestay.models.Review;
import com.homestay.models.Homestay;
import com.homestay.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByHomestayOrderByCreatedAtDesc(Homestay homestay);
    boolean existsByHomestayAndUser(Homestay homestay, User user);
} 