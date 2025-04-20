package com.homestay.controllers;

import com.homestay.models.Homestay;
import com.homestay.services.HomestayService;
import com.homestay.services.ReviewService;
import com.homestay.services.UserService;
import com.homestay.models.Review;
import com.homestay.models.User;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.security.Principal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/homestays")
public class HomestayController {

    @Autowired
    private HomestayService homestayService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Homestay>> getAllHomestays() {
        List<Homestay> homestays = homestayService.getAllHomestays();
        return homestays.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(homestays, HttpStatus.OK);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Homestay> getHomestayById(@PathVariable Long id) {
        return homestayService.getHomestayById(id)
                .map(homestay -> new ResponseEntity<>(homestay, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}")
    public String getHomestayDetail(@PathVariable Long id, Model model, Principal principal) {
        Optional<Homestay> homestay = homestayService.getHomestayById(id);
        if (homestay.isPresent()) {
            Homestay homestayObj = homestay.get();
            model.addAttribute("homestay", homestayObj);
            model.addAttribute("reviews", reviewService.getReviewsByHomestay(homestayObj));
            model.addAttribute("averageRating", reviewService.getAverageRating(homestayObj));
            
            if (principal != null) {
                User currentUser = userService.findByUsername(principal.getName());
                if (currentUser != null) {
                    model.addAttribute("hasReviewed", reviewService.hasUserReviewed(homestayObj, currentUser));
                    model.addAttribute("currentUser", currentUser);
                }
            } else {
                model.addAttribute("hasReviewed", null);
            }
            
            return "user/homestay-detail";
        }
        return "redirect:/user/home?error=notfound";
    }

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Homestay>> getHomestaysByLocation(@RequestParam String location) {
        List<Homestay> homestays = homestayService.getHomestaysByLocation(location);
        return homestays.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(homestays, HttpStatus.OK);
    }

    @GetMapping("/api/search/province")
    @ResponseBody
    public ResponseEntity<List<Homestay>> getHomestaysByProvince(@RequestParam String province) {
        List<Homestay> homestays = homestayService.getHomestaysByProvince(province);
        return homestays.isEmpty() ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(homestays, HttpStatus.OK);
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Homestay> createHomestay(@RequestBody Homestay homestay) {
        if (homestay.getName() == null || homestay.getLocation() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(homestayService.createHomestay(homestay), HttpStatus.CREATED);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Homestay> updateHomestay(@PathVariable Long id, @RequestBody Homestay updatedHomestay) {
        return homestayService.updateHomestay(id, updatedHomestay)
                .map(homestay -> new ResponseEntity<>(homestay, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteHomestay(@PathVariable Long id) {
        return homestayService.deleteHomestay(id) ? 
                new ResponseEntity<>(HttpStatus.NO_CONTENT) : 
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{id}/reviews")
    public String addReview(@PathVariable Long id, 
                          @RequestParam String comment,
                          @RequestParam int rating,
                          Principal principal) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        Optional<Homestay> homestay = homestayService.getHomestayById(id);
        if (homestay.isPresent()) {
            User user = userService.findByUsername(principal.getName());
            Review review = new Review(homestay.get(), user, comment, rating);
            reviewService.addReview(review);
        }
        return "redirect:/user/homestays/" + id;
    }
    
    @PostMapping("/reviews/{id}/edit")
    public String editReview(@PathVariable Long id, 
                           @RequestParam("editComment") String comment,
                           @RequestParam("editRating") int rating,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        
        // Check if the user is the owner of the review
        if (!reviewService.isReviewOwner(id, principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa đánh giá này");
            return "redirect:/user/homestays";
        }
        
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            review.setComment(comment);
            review.setRating(rating);
            reviewService.updateReview(review);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật đánh giá thành công");
            return "redirect:/user/homestays/" + review.getHomestay().getId();
        }
        
        redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá");
        return "redirect:/user/homestays";
    }
    
    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, 
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        
        // Check if the user is the owner of the review
        if (!reviewService.isReviewOwner(id, principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa đánh giá này");
            return "redirect:/user/homestays";
        }
        
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        if (reviewOpt.isPresent()) {
            Long homestayId = reviewOpt.get().getHomestay().getId();
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa đánh giá thành công");
            return "redirect:/user/homestays/" + homestayId;
        }
        
        redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá");
        return "redirect:/user/homestays";
    }
}
