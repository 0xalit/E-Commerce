package com.example.e_commerce.order.dto;

import com.example.e_commerce.order.entity.OrderStatus;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto implements Serializable {

    private Long id;
    private Long userId;
    private List<OrderItemResponseDto> items;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String shippingCountry;
    private String shippingCity;
    private String shippingStreet;
    private String shippingDescription;
    private LocalDateTime createdAt;
}
