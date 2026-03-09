package com.example.e_commerce.product.service;

import com.example.e_commerce.product.dto.ProductRequestDto;
import com.example.e_commerce.product.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponseDto addProduct(ProductRequestDto productRequestDto);

    ProductResponseDto getProductById(Long id);

    ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto);

    Page<ProductResponseDto> getAllProducts(Pageable pageable);

    Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable);

    void updateStock(Long productId, int quantityChange);

    Page<ProductResponseDto> searchProducts(String name, Pageable pageable);

    void deleteProduct(Long id);
}
