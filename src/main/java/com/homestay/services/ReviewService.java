package com.homestay.services;

import com.homestay.models.Review;
import com.homestay.models.Homestay;
import com.homestay.models.User;
import com.homestay.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByHomestay(Homestay homestay) {
        return reviewRepository.findByHomestayOrderByCreatedAtDesc(homestay);
    }

    public double getAverageRating(Homestay homestay) {
        List<Review> reviews = getReviewsByHomestay(homestay);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public boolean hasUserReviewed(Homestay homestay, User user) {
        return reviewRepository.existsByHomestayAndUser(homestay, user);
    }
    
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }
    
    public Review updateReview(Review review) {
        return reviewRepository.save(review);
    }
    
    public boolean deleteReview(Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isPresent()) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public boolean isReviewOwner(Long reviewId, String username) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        return review.isPresent() && review.get().getUser().getUsername().equals(username);
    }
} 