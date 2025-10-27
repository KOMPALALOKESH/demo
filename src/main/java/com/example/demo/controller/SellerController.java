package com.example.demo.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.example.demo.dto.ProductInfoDTO;
import com.example.demo.dto.ProductRequestDTO;
import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepo;
import com.example.demo.repository.ProductRepo;
import com.example.demo.repository.UserRepo;

@RestController
@RequestMapping("/auth/seller")
public class SellerController {
    
    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @GetMapping("/product")
    public ResponseEntity<List<ProductInfoDTO>> getSellerProducts(Authentication authentication) {
        List<Product> products = productRepo.findBySeller_Username(authentication.getName());
        
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

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductInfoDTO> getProductInfo(@PathVariable Integer productId) {
        Optional<Product> productOpt = productRepo.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            
            ProductInfoDTO productInfoDTO = new ProductInfoDTO(
                product.getProductId(),
                product.getProductName(),
                product.getCategory().getCategoryName(),
                product.getPrice()
            );
            
            return ResponseEntity.status(200).body(productInfoDTO);
        } else {
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping("/product")
    public ResponseEntity<Void> addSellerProduct(@RequestBody ProductRequestDTO productRequestDTO, Authentication authentication) {
        Category category = categoryRepo.findByCategoryName(productRequestDTO.getCategoryName());
        if (category == null) {
            category = new Category();
            category.setCategoryName(productRequestDTO.getCategoryName());
            category = categoryRepo.save(category);
        }

        Optional<User> seller = userRepo.findByUsername(authentication.getName());
        if(seller.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        
        Product product = new Product();
        product.setProductName(productRequestDTO.getProductName());
        product.setPrice(productRequestDTO.getPrice());
        product.setCategory(category);
        product.setSeller(seller.get());
        productRepo.save(product);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/product")
    public ResponseEntity<Void> updateSellerProduct(@RequestBody ProductRequestDTO productRequestDTO, Authentication authentication) {
        Category category = categoryRepo.findByCategoryName(productRequestDTO.getCategoryName());
        if (category == null) {
            category = new Category();
            category.setCategoryName(productRequestDTO.getCategoryName());
            category = categoryRepo.save(category);
        }

        Optional<User> seller = userRepo.findByUsername(authentication.getName());
        if (seller.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        // We assume the product name is unique and that's how we'll identify the product to be updated.
        // Alternatively, productRequestDTO could include an ID field.
        Product product = productRepo.findBySeller_Username(authentication.getName()).stream()
            .filter(p -> p.getProductName().equals(productRequestDTO.getProductName()))
            .findFirst()
            .orElse(null);

        if(product == null) {
            return ResponseEntity.status(404).build();
        }

        product.setPrice(productRequestDTO.getPrice());
        product.setCategory(category);
        productRepo.save(product);
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteSellerProduct(@PathVariable Integer productId, Authentication authentication) {
        Optional<User> seller = userRepo.findByUsername(authentication.getName());
        if (seller.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        // Only allow sellers to delete their own products
        Product product = productRepo.findBySeller_Username(authentication.getName()).stream()
            .filter(p -> p.getProductId().equals(productId))
            .findFirst()
            .orElse(null);

        if (product == null) {
            return ResponseEntity.status(404).build();
        }

        productRepo.delete(product);
        return ResponseEntity.status(200).build();
    }

}
