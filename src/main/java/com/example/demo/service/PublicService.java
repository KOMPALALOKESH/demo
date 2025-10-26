package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AuthRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.model.Product;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepo;
import com.example.demo.repository.ProductRepo;
import com.example.demo.repository.RoleRepo;
import com.example.demo.repository.UserRepo;
import com.example.demo.util.JwtUtil;

@Service
public class PublicService {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public ProductRepo productRepo;

    @Autowired
    public CategoryRepo categoryRepo;

    @Autowired
    public UserRepo userRepo;

    @Autowired
    public RoleRepo roleRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    PublicService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public List<Product> getProductsByKeyword(String keyword) {
        List<Product> products = new ArrayList<>();
        products.addAll(productRepo.findByProductNameContainingIgnoreCase(keyword));
        products.addAll(productRepo.findByCategory_CategoryName(keyword));
        return products;
    }

    public void register(AuthRequestDTO authRequestDTO) {
        
        if(authRequestDTO.getUsername() == null || authRequestDTO.getPassword() == null) {
            throw new IllegalArgumentException("Username and password must not be null");
        } else if(userRepo.findByUsername(authRequestDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        String roleName = (authRequestDTO.getRole() != null && !authRequestDTO.getRole().isBlank()) ? 
                authRequestDTO.getRole().toUpperCase() : "CONSUMER";

        Role role = roleRepo.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName(roleName);
                    return roleRepo.save(r);
                });

        User user = new User();
        user.setUsername(authRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(authRequestDTO.getPassword()));
        user.setRole(role);
        userRepo.save(user);
        return;
    }

    public AuthResponseDTO login(AuthRequestDTO authRequestDTO) {
        
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDTO.getUsername(), 
                        authRequestDTO.getPassword()
                )
            );

            User user = userRepo.findByUsername(authRequestDTO.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()))
                    .build();

            String token = jwtUtil.generateToken(userDetails);
            return new AuthResponseDTO(token, "Login successful");   
        } catch (Exception e) {
            return new AuthResponseDTO(null, "Invalid username or password");
        }

    }

}
