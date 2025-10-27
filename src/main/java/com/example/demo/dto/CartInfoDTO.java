package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartInfoDTO {
    
    private Integer cartId;
    private Double totalAmount;
    private List<CartProductInfoDTO> cartProducts;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartProductInfoDTO {
        private Integer productId;
        private String productName;
        private Double price;
        private Integer quantity;
    }
}
