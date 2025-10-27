package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.util.JwtUtil;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${security.jwt.header.authorization}")
    private String authorizationHeader;

    @Value("${security.jwt.token.prefix}")
    private String tokenPrefix;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(authorizationHeader);
        final String jwt;
        final String username;

        if(authHeader == null || !authHeader.startsWith(tokenPrefix + " ")) {
            // Authorization header is missing or does not start with the expected token prefix
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(tokenPrefix.length() + 1);

        try {
            username = jwtUtil.extractUsername(jwt);
            // Username was successfully extracted from JWT
        } catch (Exception e) {
            // JWT extraction failed (possibly malformed or expired token)
            filterChain.doFilter(request, response);
            return;
        }

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if(jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    // JWT is valid, authentication set for user
                } else {
                    // JWT is not valid for user
                }
            } catch (Exception e) {
                // User authentication failed (possibly user not found)
            }
        }

        filterChain.doFilter(request, response);
    }

}
