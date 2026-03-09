package com.example.e_commerce.user.service;

import com.example.e_commerce.user.entity.Address;
import com.example.e_commerce.user.entity.User;
import com.example.e_commerce.user.entity.UserRole;
import com.example.e_commerce.user.UserMapper;
import com.example.e_commerce.user.repo.UserRepository;
import com.example.e_commerce.user.dto.UserRequestDto;
import com.example.e_commerce.user.dto.UserResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto addUser(UserRequestDto userRequestDto) {
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            log.warn("Registration attempt with existing email: {}", userRequestDto.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userMapper.toEntity(userRequestDto);
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setRole(UserRole.USER); // Always default to USER — role cannot be set by the client

        if (userRequestDto.getAddresses() != null) {
            List<Address> addresses = userRequestDto.getAddresses().stream()
                    .map(dto -> Address.builder()
                            .country(dto.getCountry())
                            .city(dto.getCity())
                            .street(dto.getStreet())
                            .description(dto.getDescription())
                            .user(user)
                            .build())
                    .toList();
            user.setAddresses(addresses);
        }

        User savedUser = userRepository.save(user);
        log.info("New user registered: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#email")
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return userMapper.toResponseDto(user);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        existingUser.setUsername(userRequestDto.getUsername());
        existingUser.setEmail(userRequestDto.getEmail());
        existingUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));

        if (userRequestDto.getAddresses() != null) {
            existingUser.getAddresses().clear();
            userRequestDto.getAddresses().forEach(dto -> {
                Address address = Address.builder()
                        .country(dto.getCountry())
                        .city(dto.getCity())
                        .street(dto.getStreet())
                        .description(dto.getDescription())
                        .user(existingUser)
                        .build();
                existingUser.getAddresses().add(address);
            });
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: id={}", id);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDto changeUserRole(Long id, UserRole newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        UserRole previousRole = user.getRole();
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        log.info("User id={} role changed: {} → {}", id, previousRole, newRole);
        return userMapper.toResponseDto(updatedUser);
    }
}
