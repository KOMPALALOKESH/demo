package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Cart;
import com.example.demo.model.CartProduct;
import com.example.demo.model.Product;

@Repository
public interface CartProductRepo  extends JpaRepository<CartProduct, Integer> {

    Optional<CartProduct> findByCartAndProduct(Cart cart, Product product);
    
}
