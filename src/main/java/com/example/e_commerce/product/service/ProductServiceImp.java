package com.example.e_commerce.product.service;

import com.example.e_commerce.product.mapper.ProductMapper;
import com.example.e_commerce.product.dto.ProductRequestDto;
import com.example.e_commerce.product.dto.ProductResponseDto;
import com.example.e_commerce.product.entity.Category;
import com.example.e_commerce.product.entity.Product;
import com.example.e_commerce.product.repo.CategoryRepository;
import com.example.e_commerce.product.repo.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImp implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponseDto addProduct(ProductRequestDto productRequestDto) {
        Category category = categoryRepository.findById(productRequestDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found with id: " + productRequestDto.getCategoryId()));

        Product product = productMapper.toEntity(productRequestDto);
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponseDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        return productMapper.toResponseDto(product);
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(productRequestDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found with id: " + productRequestDto.getCategoryId()));

        existingProduct.setName(productRequestDto.getName());
        existingProduct.setDescription(productRequestDto.getDescription());
        existingProduct.setPrice(productRequestDto.getPrice());
        existingProduct.setStockQuantity(productRequestDto.getStockQuantity());
        existingProduct.setActive(productRequestDto.getActive());
        existingProduct.setCategory(category);

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toResponseDto(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(productMapper::toResponseDto);
    }

    @Override
    @CacheEvict(value = "products", key = "#productId")
    public void updateStock(Long productId, int quantityChange) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        int newQuantity = product.getStockQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQuantity());
        }
        product.setStockQuantity(newQuantity);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> searchProducts(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(productMapper::toResponseDto);
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
