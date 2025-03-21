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

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public List<Homestay> getAllHomestays() {
        return homestayRepository.findAll();
    }

    public Optional<Homestay> getHomestayById(Long id) {
        return homestayRepository.findById(id);
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

    /**
     * Lưu ảnh vào thư mục uploads và trả về tên file
     */
    public String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile.isEmpty()) {
            return null;
        }
    
        // Định nghĩa thư mục lưu ảnh trong thư mục static
        String uploadDir = "C:/Users/tdanh/Documents/homestay/src/main/resources/static/uploads/images";
    
        // Tạo thư mục nếu chưa tồn tại
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    
        // Tạo tên file duy nhất
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
    
        // Lưu file ảnh
        Files.write(filePath, imageFile.getBytes());
    
        // Trả về đường dẫn có thể sử dụng trên Thymeleaf
        return "/uploads/images/" + fileName;
    }
}    
