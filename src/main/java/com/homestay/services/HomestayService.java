package com.homestay.services;

import com.homestay.models.Homestay;
import com.homestay.repositories.HomestayRepository;

import jakarta.transaction.Transactional;

import com.homestay.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

@Service
public class HomestayService {
    @Autowired
    private HomestayRepository homestayRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private static final String IMAGE_UPLOAD_DIR = "C:/Users/tdanh/Documents/chotottravel/uploads/images";

    public List<Homestay> getAllHomestays() {
        return homestayRepository.findAll();
    }

    public List<Homestay> getHomestaysByLocation(String location) {
        return homestayRepository.findByLocationContainingIgnoreCase(location);
    }

    public Optional<Homestay> getHomestayById(Long id) {
        return homestayRepository.findById(id);
    }

    public Homestay createHomestay(Homestay homestay) {
        return homestayRepository.save(homestay);
    }

    public Optional<Homestay> updateHomestay(Long id, Homestay updatedHomestay) {
        return homestayRepository.findById(id).map(homestay -> {
            homestay.setName(updatedHomestay.getName());
            homestay.setLocation(updatedHomestay.getLocation());
            homestay.setDescription(updatedHomestay.getDescription());
            homestay.setAddress(updatedHomestay.getAddress());
            homestay.setPricePerNight(updatedHomestay.getPricePerNight());

            if (updatedHomestay.getImage() != null && !updatedHomestay.getImage().isEmpty()) {
                homestay.setImage(updatedHomestay.getImage());
            }

            if (updatedHomestay.getExtraImages() != null && !updatedHomestay.getExtraImages().isEmpty()) {
                homestay.setExtraImages(updatedHomestay.getExtraImages());
            }

            return homestayRepository.save(homestay);
        });
    }
    
    @Transactional
    public boolean deleteHomestay(Long id) {
        if (homestayRepository.existsById(id)) {
            // Xoá tất cả booking liên quan
            bookingRepository.deleteByHomestayId(id);
    
            // Xoá homestay
            homestayRepository.deleteById(id);
            return true;
        }
        return false;
    }
    

    public String saveImage(MultipartFile imageFile) throws IOException {
        return saveSingleImage(imageFile);
    }

    public String saveSingleImage(MultipartFile imageFile) throws IOException {
        if (imageFile.isEmpty()) return null;

        File directory = new File(IMAGE_UPLOAD_DIR);
        if (!directory.exists()) directory.mkdirs();

        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = Paths.get(IMAGE_UPLOAD_DIR, fileName);

        Files.write(filePath, imageFile.getBytes());

        return "/uploads/images/" + fileName;
    }

    public List<String> saveExtraImages(List<MultipartFile> imageFiles) throws IOException {
        List<String> savedPaths = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            if (!file.isEmpty()) {
                String path = saveSingleImage(file);
                savedPaths.add(path);
            }
        }
        return savedPaths;
    }
}
