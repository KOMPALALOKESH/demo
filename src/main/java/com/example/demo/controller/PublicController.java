package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.model.Product;
import com.example.demo.service.PublicService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController 
@RequestMapping("/public")
public class PublicController {

    @Autowired
    public PublicService publicService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody AuthRequestDTO authRequestDTO) {
        try {
            publicService.register(authRequestDTO);
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
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authResponseDTO) {
        AuthResponseDTO response = publicService.login(authResponseDTO);
        if("Invalid username or password".equals(response.getMessage())) {
            return ResponseEntity
                    .status(400)
                    .body(response);
        }
        return ResponseEntity
                .status(200)
                .body(response);

    }

    @GetMapping("/product/search")
    public ResponseEntity<List<Product>> getProductsByKeyword(@RequestParam String keyword) {
        if(keyword == null || keyword.isEmpty()) {
            return ResponseEntity
                    .status(400)
                    .build();
        }
        
        List<Product> products = publicService.getProductsByKeyword(keyword);
        return ResponseEntity
                .status(200)    
                .body(products);
    }
    
}
