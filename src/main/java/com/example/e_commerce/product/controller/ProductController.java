package com.example.e_commerce.product.controller;

import com.example.e_commerce.product.dto.ProductRequestDto;
import com.example.e_commerce.product.dto.ProductResponseDto;
import com.example.e_commerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponseDto> addProduct(@Valid @RequestBody ProductRequestDto productRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(productRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(@PathVariable Long categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> searchProducts(@RequestParam String name, Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(name, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@PathVariable Long id,
            @Valid @RequestBody ProductRequestDto productRequestDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productRequestDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(@PathVariable Long id, @RequestParam int quantityChange) {
        productService.updateStock(id, quantityChange);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
