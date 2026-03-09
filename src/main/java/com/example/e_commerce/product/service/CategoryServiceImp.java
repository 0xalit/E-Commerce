package com.example.e_commerce.product.service;

import com.example.e_commerce.product.mapper.CategoryMapper;
import com.example.e_commerce.product.dto.CategoryRequestDto;
import com.example.e_commerce.product.dto.CategoryResponseDto;
import com.example.e_commerce.product.entity.Category;
import com.example.e_commerce.product.repo.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImp implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + dto.getName());
        }
        Category category = categoryMapper.toEntity(dto);
        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        return categoryMapper.toResponseDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toResponseDto);
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
        Category category = findById(id);
        if (categoryRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new IllegalArgumentException("Category name already exists: " + dto.getName());
        }
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }
}
