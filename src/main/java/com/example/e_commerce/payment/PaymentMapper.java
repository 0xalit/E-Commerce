package com.example.e_commerce.payment;

import com.example.e_commerce.payment.dto.PaymentResponseDto;
import com.example.e_commerce.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(target = "clientSecret", ignore = true)
    PaymentResponseDto toResponseDto(Payment payment);
}
