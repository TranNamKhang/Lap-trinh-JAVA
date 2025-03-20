package com.homestay.services;

import com.homestay.models.Review;
import com.homestay.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsByHomestayId(Long homestayId) {
        return reviewRepository.findByHomestayId(homestayId);
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }
}
