package com.homestay.services;

import com.homestay.models.Homestay;
import com.homestay.repositories.HomestayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HomestayService {

    @Autowired
    private HomestayRepository homestayRepository;

    private static final String UPLOAD_DIR = "D:/01 THANH IT/HK2 NAM 2/Lap trinh Java/ProjectJavaHomestay/uploads";

    public List<Homestay> getAllHomestays() {
        return homestayRepository.findAll();
    }


    public List<Homestay> getHomestaysByLocation(String location) {
        return homestayRepository.findByLocationContainingIgnoreCase(location);
    }

    public Homestay createHomestay(Homestay homestay) {
        return homestayRepository.save(homestay);
    }

    public Optional<Homestay> updateHomestay(Long id, Homestay updatedHomestay) {
        return homestayRepository.findById(id).map(homestay -> {
            homestay.setName(updatedHomestay.getName());
            homestay.setLocation(updatedHomestay.getLocation());

            // Cập nhật hình ảnh nếu có
            if (updatedHomestay.getImage() != null && !updatedHomestay.getImage().isEmpty()) {
                homestay.setImage(updatedHomestay.getImage());
            }

            return homestayRepository.save(homestay);
        });
    }

    public boolean deleteHomestay(Long id) {
        if (homestayRepository.existsById(id)) {
            homestayRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile.isEmpty()) {
            return null;
        }
    
        String uploadDir = "D:/01 THANH IT/HK2 NAM 2/Lap trinh Java/ProjectJavaHomestay/uploads/images";
    
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
    
        Files.write(filePath, imageFile.getBytes());
    
        return "/uploads/images/" + fileName;
    }

    public List<Homestay> findByCategoryId(Long categoryId) {
        return homestayRepository.findByCategoryId(categoryId);
    }

    public Optional<Homestay> getHomestayById(Long id) {
        return homestayRepository.findById(id);
    }
}    
