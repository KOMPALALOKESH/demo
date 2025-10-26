package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Cart;
import com.example.demo.model.CartProduct;
import com.example.demo.model.Product;
import com.example.demo.repository.CartProductRepo;
import com.example.demo.repository.CartRepo;

@RestController
@RequestMapping("/auth/consumer")
public class ConsumerController {

    @Autowired
    private CartProductRepo cartProductRepo;
    
    @Autowired
    private CartRepo CartRepo;
    
    @GetMapping("/cart")
    public ResponseEntity<Cart> getCartInfo(Authentication authentication) {
        Cart cart = CartRepo.findByUser_Username(authentication.getName());

        return ResponseEntity.status(200).body(cart);
    }

    @PostMapping("/cart")
    public ResponseEntity<Void> addProductToCart(@RequestBody Product product, Authentication authentication) {
        Cart cart = CartRepo.findByUser_Username(authentication.getName());

        if (cart.getCartProducts().stream().anyMatch(cp -> cp.getProduct().equals(product))) {
            return ResponseEntity.status(409).build();
        }
        CartProduct cartProduct = new CartProduct();
        cartProduct.setProduct(product);
        cartProduct.setCart(cart);
        cartProductRepo.save(cartProduct);
        return ResponseEntity.status(200).build();
    }
    
    @PutMapping("/cart")
    public ResponseEntity<Void> updateCart(@RequestBody CartProduct cartProduct, Authentication authentication) {
        Cart cart = CartRepo.findByUser_Username(authentication.getName());
        
        cartProduct.setCart(cart);
        cartProduct.setProduct(cartProduct.getProduct());
        cartProductRepo.save(cartProduct);
        
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Void> removeProductInCart(@RequestBody Product product, Authentication authentication) {
        Cart cart = CartRepo.findByUser_Username(authentication.getName());

        if (cart.getCartProducts().stream().noneMatch(cp -> cp.getProduct().equals(product))) {
            return ResponseEntity.status(409).build();
        }

        cart.getCartProducts().stream()
            .filter(cp -> cp.getProduct().equals(product))
            .forEach(cp -> cartProductRepo.delete(cp));

        return ResponseEntity.status(200).build();
    }

}
