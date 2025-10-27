package com.example.demo.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.RoleDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepo;

@RestController
@RequestMapping("/auth/admin")
public class AdminController {

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/consumers")
    public ResponseEntity<List<UserDTO>> getAllConsumers() {
        List<User> consumers = userRepo.findByRole_RoleName("CONSUMER");
        List<UserDTO> consumerDTOs = consumers.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.status(200).body(consumerDTOs);
    }

    @GetMapping("/sellers")
    public ResponseEntity<List<UserDTO>> getAllSellers() {
        List<User> sellers = userRepo.findByRole_RoleName("SELLER");
        List<UserDTO> sellerDTOs = sellers.stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        return ResponseEntity.status(200).body(sellerDTOs);
    }

    @DeleteMapping("/consumer/{username}")
    public ResponseEntity<Void> deleteConsumer(@PathVariable String username) {
        Optional<User> userOptional = userRepo.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOptional.get();
        if (!"CONSUMER".equals(user.getRole().getRoleName())) {
            return ResponseEntity.status(403).build(); 
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
        if (!"SELLER".equals(user.getRole().getRoleName())) {
            return ResponseEntity.status(403).build();
        }

        userRepo.delete(user);
        return ResponseEntity.status(200).build();
    }

    private UserDTO convertToUserDTO(User user) {
        RoleDTO roleDTO = new RoleDTO(
            user.getRole().getRoleId(),
            user.getRole().getRoleName()
        );
        return new UserDTO(
            user.getUserId(),
            user.getUsername(),
            user.getPassword(),
            roleDTO
        );
    }

}
