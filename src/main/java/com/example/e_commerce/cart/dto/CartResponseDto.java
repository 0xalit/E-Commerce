package com.example.e_commerce.cart.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto implements Serializable {

    private Long id;
    private Long userId;
    private List<CartItemResponseDto> items;
    private BigDecimal totalPrice;
}
