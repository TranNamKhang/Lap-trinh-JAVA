package com.homestay.controllers;

import com.homestay.models.Review;
import com.homestay.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @GetMapping("/{homestayId}")
    public List<Review> getReviewsByHomestayId(@PathVariable Long homestayId) {
        return reviewService.getReviewsByHomestayId(homestayId);
    }

    @PostMapping("/")
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }
}
