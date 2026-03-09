package com.example.e_commerce.product.mapper;

import com.example.e_commerce.product.dto.CategoryRequestDto;
import com.example.e_commerce.product.dto.CategoryResponseDto;
import com.example.e_commerce.product.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // 'id' is DB-generated — never set from request data
    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryRequestDto dto);

    CategoryResponseDto toResponseDto(Category category);
}
