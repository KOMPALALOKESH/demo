package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepo;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/auth/admin")
public class AdminController {

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/consumers")
    public ResponseEntity<List<User>> getAllConsumers() {
        System.out.println("Fetching all consumers...");
        log.info("start: fetching all customers..");
        List<User> consumers = userRepo.findByRole_RoleName("CONSUMER");
        log.info("end: fetching all customers.");
        return ResponseEntity.status(200).body(consumers);
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<User>> getAllSellers() {
        List<User> sellers = userRepo.findByRole_RoleName("ROLE_SELLER");
        return ResponseEntity.status(200).body(sellers);
    }

    @DeleteMapping("/consumer/{username}")
    public ResponseEntity<Void> deleteConsumer(@PathVariable String username) {
        Optional<User> userOptional = userRepo.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOptional.get();
        if (!"ROLE_CONSUMER".equals(user.getRole().getRoleName())) {
            return ResponseEntity.status(403).build(); // Forbidden if not consumer
        }

        userRepo.delete(user);
        return ResponseEntity.status(200).build();
    }


    @DeleteMapping("/seller/{username}")
    public ResponseEntity<Void> deleteSeller(@PathVariable String username) {
        Optional<User> userOptional = userRepo.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOptional.get();
        if (!"ROLE_SELLER".equals(user.getRole().getRoleName())) {
            return ResponseEntity.status(403).build();
        }

        userRepo.delete(user);
        return ResponseEntity.status(200).build();
    }

}
