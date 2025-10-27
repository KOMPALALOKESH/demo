package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.ProductInfoDTO;
import com.example.demo.model.Product;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepo;
import com.example.demo.repository.ProductRepo;
import com.example.demo.repository.RoleRepo;
import com.example.demo.repository.UserRepo;
import com.example.demo.util.JwtUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController 
@RequestMapping("/public")
public class PublicController {

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

    public PublicController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody AuthRequestDTO authRequestDTO) {
        try {
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
            
            return ResponseEntity
                    .status(201)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(400)
                    .build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequestDTO) {
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
            AuthResponseDTO response = new AuthResponseDTO(token, "Login successful");
            return ResponseEntity
                    .status(200)
                    .body(response);
        } catch (Exception e) {
            AuthResponseDTO response = new AuthResponseDTO(null, "Invalid username or password");
            return ResponseEntity
                    .status(400)
                    .body(response);
        }
    }

    @GetMapping("/product/search")
    public ResponseEntity<List<ProductInfoDTO>> getProductsByKeyword(@RequestParam String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return ResponseEntity.status(400).build();
        }

        List<Product> products = new ArrayList<>();
        products.addAll(productRepo.findByProductNameContainingIgnoreCase(keyword));
        products.addAll(productRepo.findByCategory_CategoryName(keyword));

        List<ProductInfoDTO> productInfoDTOs = products.stream()
            .map(product -> new ProductInfoDTO(
                product.getProductId(),
                product.getProductName(),
                product.getCategory().getCategoryName(),
                product.getPrice()
            ))
            .collect(Collectors.toList());

        return ResponseEntity
            .status(200)
            .body(productInfoDTOs);
    }

    @GetMapping("/listProductsMenu")
    public ResponseEntity<List<ProductInfoDTO>> getAllProductsInfo() {
        List<Product> products = productRepo.findAll();
        List<ProductInfoDTO> productInfoDTOs = products.stream()
            .map(product -> new ProductInfoDTO(
                product.getProductId(),
                product.getProductName(),
                product.getCategory().getCategoryName(),
                product.getPrice()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.status(200).body(productInfoDTOs);
    }
    
}
