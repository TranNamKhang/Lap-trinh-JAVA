package com.homestay.services;

import com.homestay.models.Category;
import com.homestay.models.Homestay;
import com.homestay.repositories.BookingRepository;
import com.homestay.repositories.CategoryRepository;
import com.homestay.repositories.HomestayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service

public class CategoryService {
    @Autowired
    private HomestayRepository homestayRepository;

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        long count = homestayRepository.countByCategoryId(id);
        if (count > 0) {
            throw new IllegalStateException("Không thể xóa danh mục vì đang được sử dụng.");
        }
        categoryRepository.deleteById(id);
    }
}