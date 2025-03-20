package com.homestay.services;

import com.homestay.models.Homestay;
import com.homestay.repositories.HomestayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HomestayService {
    @Autowired
    private HomestayRepository homestayRepository;

    public List<Homestay> getAllHomestays() {
        return homestayRepository.findAll();
    }

    public Optional<Homestay> getHomestayById(Long id) {
        return homestayRepository.findById(id);
    }

    public List<Homestay> getHomestaysByLocation(String location) {
        return homestayRepository.findByLocationContaining(location);
    }

    public Homestay createHomestay(Homestay homestay) {
        return homestayRepository.save(homestay);
    }

    // Sửa lỗi: Phương thức saveHomestay không cần return nếu khai báo kiểu void
    public void saveHomestay(Homestay homestay) {
        homestayRepository.save(homestay);
    }

    // Cập nhật phương thức deleteHomestay
    public void deleteHomestay(Long id) {
        homestayRepository.deleteById(id);
    }
}
