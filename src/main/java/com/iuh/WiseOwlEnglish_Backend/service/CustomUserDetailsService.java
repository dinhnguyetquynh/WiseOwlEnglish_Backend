package com.iuh.WiseOwlEnglish_Backend.service;

import com.iuh.WiseOwlEnglish_Backend.enums.AccountStatus;
import com.iuh.WiseOwlEnglish_Backend.model.UserAccount;
import com.iuh.WiseOwlEnglish_Backend.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository repo;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        boolean enabled = u.getStatus() == AccountStatus.VERIFIED;
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRoleAccount().name()));

        return new MyUserDetails(
                u.getId(),                // 👈 nhét id vào principal
                u.getEmail(),
                u.getPasswordHash(),
                enabled,
                true,                     // accountNonExpired
                true,                     // credentialsNonExpired
                u.getStatus() != AccountStatus.LOCKED,
                authorities
        );
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        UserAccount u = repo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User id not found"));

        boolean enabled = u.getStatus() == AccountStatus.VERIFIED;
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRoleAccount().name()));

        return new MyUserDetails(
                u.getId(),
                u.getEmail(),
                u.getPasswordHash(),
                enabled,
                true,
                true,
                u.getStatus() != AccountStatus.LOCKED,
                authorities
        );
    }
}
