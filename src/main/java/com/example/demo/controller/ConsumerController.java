package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

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

import com.example.demo.dto.CartInfoDTO;
import com.example.demo.dto.CartProductDTO;
import com.example.demo.model.Cart;
import com.example.demo.model.CartProduct;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.CartProductRepo;
import com.example.demo.repository.CartRepo;
import com.example.demo.repository.ProductRepo;
import com.example.demo.repository.UserRepo;

@RestController
@RequestMapping("/auth/consumer")
public class ConsumerController {

    @Autowired
    private CartProductRepo cartProductRepo;
    
    @Autowired
    private CartRepo CartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;
    
    @GetMapping("/cart")
    public ResponseEntity<CartInfoDTO> getCartInfo(Authentication authentication) {
        Cart cart = CartRepo.findByUser_Username(authentication.getName());
        
        if (cart == null) {
            return ResponseEntity.status(404).build();
        }
        
        // Calculate total amount
        Double totalAmount = calculateTotalAmount(cart);
        cart.setTotalAmount(totalAmount);
        CartRepo.save(cart);
        
        // Convert to DTO
        List<CartInfoDTO.CartProductInfoDTO> cartProductDTOs = cart.getCartProducts().stream()
            .map(cp -> new CartInfoDTO.CartProductInfoDTO(
                cp.getProduct().getProductId(),
                cp.getProduct().getProductName(),
                cp.getProduct().getPrice(),
                cp.getQuantity()
            ))
            .collect(java.util.stream.Collectors.toList());
        
        CartInfoDTO cartInfoDTO = new CartInfoDTO(
            cart.getCartId(),
            totalAmount,
            cartProductDTOs
        );
        
        return ResponseEntity.status(200).body(cartInfoDTO);
    }
    
    private Double calculateTotalAmount(Cart cart) {
        double sum = 0.0;
        for (CartProduct cp : cart.getCartProducts()) {
            sum += cp.getProduct().getPrice() * cp.getQuantity();
        }
        return sum;
    }

    @PostMapping("/cart")
    public ResponseEntity<Void> addProductToCart(@RequestBody CartProductDTO cartProductDTO, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userRepo.findByUsername(username);
        Product product = productRepo.findByProductId(cartProductDTO.getProductId());
        if (product == null) {
            return ResponseEntity.status(404).build();
        }
        Cart cart = CartRepo.findByUser_Username(username);
        if (cart == null) {
            // If no cart exists, create one
            cart = new Cart();
            cart.setUser(user.get());
            cart = CartRepo.save(cart);
        }

        Optional<CartProduct> existingCartProduct = cartProductRepo.findByCartAndProduct(cart, product);

        if (existingCartProduct.isPresent()) {
            // If product is already in the cart, return error
            return ResponseEntity.status(409).build();
        } else {
            CartProduct cartProduct = new CartProduct();
            cartProduct.setProduct(product);
            cartProduct.setCart(cart);
            cartProduct.setQuantity(cartProductDTO.getQuantity());
            cartProductRepo.save(cartProduct);
        }
        
        // Update total amount
        Double totalAmount = calculateTotalAmount(cart);
        cart.setTotalAmount(totalAmount);
        CartRepo.save(cart);
        
        return ResponseEntity.status(200).build();
    }
    
    @PutMapping("/cart")
    public ResponseEntity<Void> updateCart(@RequestBody CartProductDTO cartProductdto, Authentication authentication) {
        Optional<User> user = userRepo.findByUsername(authentication.getName());
        
        Cart cart = CartRepo.findByUser_Username(user.get().getUsername());
        if (cart == null) {
            Cart newCart = new Cart();
            newCart.setUser(user.get());
            cart = CartRepo.save(newCart);
        }

        Product product = productRepo.findByProductId(cartProductdto.getProductId());
        if (product == null) {
            return ResponseEntity.status(404).build();
        }

        Optional<CartProduct> existingCartProduct = cartProductRepo.findByCartAndProduct(cart, product);

        if (existingCartProduct.isPresent()) {
            existingCartProduct.get().setQuantity(cartProductdto.getQuantity());
            cartProductRepo.save(existingCartProduct.get());
        } else {
            CartProduct newCartProduct = new CartProduct();
            newCartProduct.setCart(cart);
            newCartProduct.setProduct(product);
            newCartProduct.setQuantity(cartProductdto.getQuantity());
            cartProductRepo.save(newCartProduct);
        }

        // Update total amount
        Double totalAmount = calculateTotalAmount(cart);
        cart.setTotalAmount(totalAmount);
        CartRepo.save(cart);

        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/cart")
    public ResponseEntity<Void> removeProductInCart(@RequestBody CartProductDTO cartProductDTO, Authentication authentication) {
        Optional<User> user = userRepo.findByUsername(authentication.getName());

        Cart cart = CartRepo.findByUser_Username(user.get().getUsername());
        if (cart == null) {
            return ResponseEntity.status(404).build();
        }

        Product product = productRepo.findByProductId(cartProductDTO.getProductId());
        if (product == null) {
            return ResponseEntity.status(404).build();
        }

        Optional<CartProduct> cartProductOpt = cartProductRepo.findByCartAndProduct(cart, product);

        if (cartProductOpt.isPresent()) {
            cartProductRepo.delete(cartProductOpt.get());
            
            // Update total amount
            Double totalAmount = calculateTotalAmount(cart);
            cart.setTotalAmount(totalAmount);
            CartRepo.save(cart);
            
            return ResponseEntity.status(200).build();
        } else {
            return ResponseEntity.status(409).build();
        }
    }

}
