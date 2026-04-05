package com.phishguard.controller;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.entity.User;
import com.phishguard.service.UserService;

@RestController
@RequestMapping("/users")
public class HelloController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        // Validate required fields
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Email is required\"}");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Password is required\"}");
        }
        // Prevent duplicate accounts
        Optional<User> existing = userService.findByEmail(user.getEmail().trim().toLowerCase());
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body("{\"error\": \"Email already registered\"}");
        }
        user.setEmail(user.getEmail().trim().toLowerCase());
        // createUser() hashes the password with BCrypt before saving
        User saved = userService.createUser(user);
        return ResponseEntity.status(201).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        String inputEmail    = loginRequest.getEmail()    == null ? "" : loginRequest.getEmail().trim().toLowerCase();
        String inputPassword = loginRequest.getPassword() == null ? "" : loginRequest.getPassword();

        // Corporate rule: always look up by email, never reveal which field is wrong
        Optional<User> existingUser = userService.findByEmail(inputEmail);

        // BCrypt verification: matches(plainText, hashedFromDB)
        if (existingUser.isPresent() && userService.checkPassword(inputPassword, existingUser.get().getPassword())) {
            return ResponseEntity.ok(existingUser.get());
        }

        // Generic message — never say "email not found" or "wrong password"
        return ResponseEntity.status(401).body("{\"error\": \"Invalid credentials\"}");
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);

        if (deleted) {
            return ResponseEntity.ok("User deleted successfully");
        }

        return ResponseEntity.notFound().build();
    }
}