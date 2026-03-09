package com.example.e_commerce.product.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto implements Serializable {

    private Long id;
    private String name;
    private String description;
}
