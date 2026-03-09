package com.example.e_commerce.user.dto;

import com.example.e_commerce.user.entity.UserRole;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto implements Serializable {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private List<AddressResponseDto> addresses;
    private LocalDateTime createdAt;
}
