package com.iuh.WiseOwlEnglish_Backend.config;

import com.iuh.WiseOwlEnglish_Backend.service.CustomUserDetailsService;
import com.iuh.WiseOwlEnglish_Backend.service.MyUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        final String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        final String token = authHeader.substring(7);
        try {
            // ⬇️ Nếu token invalid/expired, các hàm dưới đây sẽ ném exception
            Long userId = jwtService.extractUserId(token); // sub = id
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtService.isValid(token)) {

                var user = userDetailsService.loadUserById(userId);

                var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(req, res);
        } catch (io.jsonwebtoken.ExpiredJwtException |
                 io.jsonwebtoken.MalformedJwtException |
                 io.jsonwebtoken.security.SignatureException e) {
            // ⬇️ QUAN TRỌNG: kết sớm với 401 để FE biết mà refresh
            SecurityContextHolder.clearContext();
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired or invalid");
        } catch (Exception e) {
            // các lỗi khác về JWT cũng coi như 401
            SecurityContextHolder.clearContext();
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}


