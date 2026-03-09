package com.example.e_commerce.user.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDto implements Serializable {

    private Long id;
    private String country;
    private String city;
    private String street;
    private String description;
}
