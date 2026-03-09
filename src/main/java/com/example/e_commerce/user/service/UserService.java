package com.example.e_commerce.user.service;

import com.example.e_commerce.user.dto.UserRequestDto;
import com.example.e_commerce.user.dto.UserResponseDto;
import com.example.e_commerce.user.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponseDto addUser(UserRequestDto userRequestDto);

    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByEmail(String email);

    UserResponseDto updateUser(Long id, UserRequestDto userRequestDto);

    Page<UserResponseDto> getAllUsers(Pageable pageable);

    void deleteUser(Long id);

    UserResponseDto changeUserRole(Long id, UserRole newRole);

}
