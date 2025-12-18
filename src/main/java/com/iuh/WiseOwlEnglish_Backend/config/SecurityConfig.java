package com.iuh.WiseOwlEnglish_Backend.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;            // đảm bảo JwtAuthFilter có @Component
    private final UserDetailsService userDetailsService;  // CustomUserDetailsService @Service

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**","/api/learn/lessons/for-guest/**").permitAll()   // login, register, verify-otp, refresh...
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // ⬇️ Chưa xác thực / JWT invalid/expired => 401
                        .authenticationEntryPoint((req, res, e) -> {
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        // ⬇️ Đã xác thực nhưng thiếu quyền => 403
                        .accessDeniedHandler((req, res, e) -> {
                            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        })
                )
                .authenticationProvider(authenticationProvider())   // <-- dùng bean bên dưới
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // đừng dùng encoder “trả y nguyên” nữa
    }

    // Nếu bạn cần inject AuthenticationManager ở nơi khác (vd. AuthController / Service)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
    // 🔑 CORS config: Cập nhật thêm domain Vercel
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var cfg = new org.springframework.web.cors.CorsConfiguration();

        // 👇 THAY ĐỔI QUAN TRỌNG Ở ĐÂY:
        cfg.setAllowedOrigins(java.util.List.of(
                "http://localhost:5173",                        // Giữ lại để chạy local
                "https://wise-owl-english-frontend.vercel.app"  // <-- Thêm dòng này (Link Vercel của bạn)
        ));

        cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(java.util.List.of("Authorization","Content-Type","Accept"));
        cfg.setExposedHeaders(java.util.List.of("Location"));

        // Lưu ý: Nếu sau này bạn dùng Cookie thì phải sửa thành true.
        // Hiện tại dùng Header Authorization thì false vẫn ổn.
        cfg.setAllowCredentials(false);

        cfg.setMaxAge(3600L);

        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
