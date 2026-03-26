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
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Email is required\"}");
        }
        // Check duplicate email
        Optional<User> existing = userService.findByEmail(user.getEmail().trim());
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body("{\"error\": \"Email already registered\"}");
        }
        user.setEmail(user.getEmail().trim());
        User saved = userService.createUser(user);
        return ResponseEntity.status(201).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        String inputEmail = loginRequest.getEmail() == null ? "" : loginRequest.getEmail().trim();
        String inputPassword = loginRequest.getPassword() == null ? "" : loginRequest.getPassword().trim();

        // Try exact match first, then lowercase
        Optional<User> existingUser = userService.findByEmail(inputEmail);
        if (existingUser.isEmpty()) {
            existingUser = userService.findByEmail(inputEmail.toLowerCase());
        }

        System.out.println("Input Email: " + inputEmail);
        System.out.println("Input Password: " + inputPassword);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            String dbEmail = user.getEmail() == null ? "" : user.getEmail().trim();
            String dbPassword = user.getPassword() == null ? "" : user.getPassword().trim();

            System.out.println("DB Email: " + dbEmail);
            System.out.println("DB Password: " + dbPassword);

            if (dbPassword.equals(inputPassword)) {
                // ✅ Return the User object so frontend gets data.id
                return ResponseEntity.ok(user);
            }
        }

        return ResponseEntity.status(401).body("{\"error\": \"Invalid email or password\"}");
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