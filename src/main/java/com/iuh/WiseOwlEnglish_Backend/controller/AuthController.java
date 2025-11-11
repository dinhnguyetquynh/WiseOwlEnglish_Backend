package com.iuh.WiseOwlEnglish_Backend.controller;

import com.iuh.WiseOwlEnglish_Backend.config.JwtService;
import com.iuh.WiseOwlEnglish_Backend.dto.request.LoginRequest;
import com.iuh.WiseOwlEnglish_Backend.dto.request.ResendOtpReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.UserAccountReq;
import com.iuh.WiseOwlEnglish_Backend.dto.request.VerifyOtpReq;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.AdminAccountRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.LoginRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.TokenRes;
import com.iuh.WiseOwlEnglish_Backend.dto.respone.UserAccountRes;
import com.iuh.WiseOwlEnglish_Backend.enums.RoleAccount;
import com.iuh.WiseOwlEnglish_Backend.model.UserAccount;
import com.iuh.WiseOwlEnglish_Backend.repository.LearnerProfileRepository;
import com.iuh.WiseOwlEnglish_Backend.service.AuthService;
import com.iuh.WiseOwlEnglish_Backend.service.CustomUserDetailsService;
import com.iuh.WiseOwlEnglish_Backend.service.MyUserDetails;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final LearnerProfileRepository profileRepo;

    @PostMapping("/register")
    public ResponseEntity<UserAccountRes> register(@Validated @RequestBody UserAccountReq req) {
        UserAccount u = authService.createUserAccount(req);

        // Create location header
        //get current url + /{id} , replace {id} with u.getId(), convert to URI
        // http://localhost:8080/api/auth + /{id} => http://localhost:8080/api/auth/1
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(u.getId())
                .toUri();

        UserAccountRes body = new UserAccountRes(
                u.getId(),
                u.getEmail(),
                u.getRoleAccount().name(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );

        return ResponseEntity.created(location).body(body); // 201 + Location header
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verify(@RequestBody VerifyOtpReq req) {
        authService.verifyOtp(req.getEmail(), req.getOtp());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Void> resend(@RequestBody ResendOtpReq req) {
        authService.resendOtp(req.getEmail());
        return ResponseEntity.noContent().build();
    }

    //API LOGIN CHO ADMIN VA LEARNER
    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(@Validated @RequestBody LoginRequest req) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        var principal = (MyUserDetails) auth.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        claims.put("email", principal.getUsername()); // tiện debug/giám sát

        String access = jwtService.generateToken(String.valueOf(principal.getId()), claims, Duration.ofMinutes(15));
        String refresh = jwtService.generateToken(
                String.valueOf(principal.getId()),
                Map.of("typ", "refresh"),
                Duration.ofDays(30)
        );
        final long userId = principal.getId();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

        int count = 0;
        boolean hasProfiles = false;
        if (!isAdmin) {
            count = profileRepo.countByUserAccount_Id(userId);
            hasProfiles = count > 0;
        }

        String authority = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");
        RoleAccount roleAccount = RoleAccount.fromAuthority(authority);
        return ResponseEntity.ok(new LoginRes(access, refresh, hasProfiles, count,roleAccount));
    }


    @PostMapping("/refresh")
    public ResponseEntity<TokenRes> refresh(@RequestBody Map<String, String> body) {
        System.out.println("Refresh token request received");
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            // 1) Kiểm tra hạn & đúng loại refresh
            if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2) Lấy userId từ sub
            Long userId = jwtService.extractUserId(refreshToken);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 3) Load user theo id (đảm bảo trạng thái hiện tại: enabled/locked…)
            var user = (MyUserDetails) customUserDetailsService.loadUserById(userId);

            // 4) Tạo access token mới (sub = userId)
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());
            claims.put("email", user.getUsername()); // tiện debug/giám sát

            String newAccess = jwtService.generateToken(
                    String.valueOf(user.getId()),
                    claims,
                    Duration.ofMinutes(15)
            );

            // 5) Trả access mới, giữ nguyên refresh cũ (no rotation)
            return ResponseEntity.ok(new TokenRes(newAccess, refreshToken));
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
