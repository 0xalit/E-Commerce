package com.example.e_commerce.security;

import com.example.e_commerce.user.entity.User;
import com.example.e_commerce.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found: " + email));

        String roleName = user.getRole() != null ? user.getRole().name() : "USER";
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));

        boolean enabled = true;
        boolean accountNonLocked = true;
        boolean credentialsNonExpired = true;
        boolean accountNonExpired = true;

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(!accountNonExpired)
                .accountLocked(!accountNonLocked)
                .credentialsExpired(!credentialsNonExpired)
                .disabled(!enabled)
                .build();
    }
}
