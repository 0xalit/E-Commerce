package com.example.e_commerce.product.service;

import com.example.e_commerce.product.dto.CategoryRequestDto;
import com.example.e_commerce.product.dto.CategoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponseDto createCategory(CategoryRequestDto dto);

    CategoryResponseDto getCategoryById(Long id);

    Page<CategoryResponseDto> getAllCategories(Pageable pageable);

    CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto);

    void deleteCategory(Long id);
}
