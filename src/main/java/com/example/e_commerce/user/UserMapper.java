package com.example.e_commerce.user;

import com.example.e_commerce.user.dto.AddressRequestDto;
import com.example.e_commerce.user.dto.UserRequestDto;
import com.example.e_commerce.user.dto.UserResponseDto;
import com.example.e_commerce.user.entity.Address;
import com.example.e_commerce.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 'role' and 'password' are set explicitly in the service — ignore them here
    // 'id', 'createdAt', and 'version' are managed by the DB/JPA — never set from
    // request data
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    User toEntity(UserRequestDto dto);

    // 'id', 'user', and 'version' on Address are DB/JPA-managed — never set from
    // request data. 'user' is linked explicitly in the service.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "version", ignore = true)
    Address toEntity(AddressRequestDto dto);

    UserResponseDto toResponseDto(User user);
}
