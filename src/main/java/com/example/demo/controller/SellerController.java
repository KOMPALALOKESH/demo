package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepo;

@RestController
@RequestMapping("/auth/seller")
public class SellerController {
    
    @Autowired
    private ProductRepo productRepo;

    @GetMapping("/product")
    public ResponseEntity<List<Product>> getSellerProducts(Authentication authentication) {
        List<Product> products = productRepo.findBySeller_Username(authentication.getName());

        return ResponseEntity.status(200).body(products);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Product> getProductInfo(@PathVariable Integer productId) {
        Product product = productRepo.findById(productId).orElse(new Product());

        return ResponseEntity.status(200).body(product);
    }

    @PostMapping("/product")
    public ResponseEntity<Void> addSellerProduct(@RequestBody Product product) {
        User seller = new User();
        product.setSeller(seller);
        com.example.demo.model.Category category = new com.example.demo.model.Category();
        category.setCategoryId(product.getCategory().getCategoryId());
        product.setCategory(category);
        productRepo.save(product);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/product")
    public ResponseEntity<Void> updateSellerProduct(@RequestBody Product product) {
        User seller = new User();
        product.setSeller(seller);
        com.example.demo.model.Category category = new com.example.demo.model.Category();
        category.setCategoryId(product.getCategory().getCategoryId());
        product.setCategory(category);
        productRepo.save(product);
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteSellerProduct(@PathVariable Integer productId) {
        if(productRepo.findById(productId).isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        Product product = productRepo.findById(productId).get();
        productRepo.delete(product);
        return ResponseEntity.status(200).build();
    }

}
