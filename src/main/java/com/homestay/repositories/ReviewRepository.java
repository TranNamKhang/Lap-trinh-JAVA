package com.homestay.repositories;

import com.homestay.models.Review;
import com.homestay.models.Homestay;
import com.homestay.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByHomestayOrderByCreatedAtDesc(Homestay homestay);
    boolean existsByHomestayAndUser(Homestay homestay, User user);
    
    @Modifying
    @Query("DELETE FROM Review r WHERE r.user.id = ?1")
    void deleteByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM Review r WHERE r.homestay.id = ?1")
    void deleteByHomestayId(Long homestayId);
} 