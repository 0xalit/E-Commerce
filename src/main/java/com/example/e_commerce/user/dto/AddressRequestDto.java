package com.example.e_commerce.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDto {

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Street is required")
    private String street;

    @Size(max = 200, message = "Address description must not exceed 200 characters")
    private String description;
}
