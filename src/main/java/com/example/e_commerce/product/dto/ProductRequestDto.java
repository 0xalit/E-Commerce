package com.example.e_commerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    @Size(max = 120, message = "Product name must not exceed 120 characters")
    private String name;

    @Size(max = 500, message = "Product description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be at least 0")
    private Integer stockQuantity;

    @NotNull(message = "Active status is required")
    private Boolean active;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
