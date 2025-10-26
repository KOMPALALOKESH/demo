package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Cart;


@Repository
public interface CartRepo extends JpaRepository<Cart, Integer> {

    public Cart findByUser_Username(String user_Username);
}
