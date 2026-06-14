package com.albert.patientsystem.security;

import com.albert.patientsystem.entity.User;
import com.albert.patientsystem.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges our {@link User} entity into Spring Security's UserDetails
 * contract. The user's role is prefixed with "ROLE_" so it works with
 * {@code @PreAuthorize("hasRole('ADMIN')")}.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
            u.getUsername(),
            u.getPasswordHash(),
            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))
        );
    }
}
