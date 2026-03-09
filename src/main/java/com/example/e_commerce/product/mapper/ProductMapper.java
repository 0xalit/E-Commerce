package com.example.e_commerce.product.mapper;

import com.example.e_commerce.product.dto.ProductRequestDto;
import com.example.e_commerce.product.dto.ProductResponseDto;
import com.example.e_commerce.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // 'id', 'createdAt', 'version' are DB/JPA-managed — never set from request data
    // 'category' is resolved from 'categoryId' and set explicitly in the service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequestDto dto);

    @Mapping(target = "categoryName", source = "category.name")
    ProductResponseDto toResponseDto(Product product);
}
