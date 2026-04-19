package com.phishguard.controller;

import java.util.HashMap;
import com.phishguard.security.TokenBlacklist;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.phishguard.entity.User;
import com.phishguard.security.JwtUtil;
import com.phishguard.service.EmailService;
import com.phishguard.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

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

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Registration failed. Please try again."));
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Registration failed. Please try again."));
        }

        Optional<User> existing = userService.findByEmail(user.getEmail().trim().toLowerCase());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Registration failed. Please try again."));
        }

        user.setEmail(user.getEmail().trim().toLowerCase());
        String token = java.util.UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setEmailVerified(false);

        User saved = userService.createUser(user);
        emailService.sendVerificationEmail(saved.getEmail(), token);

        return ResponseEntity.status(201)
            .body(Map.of("message", "Registration successful! Please check your email to verify your account."));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = userService.findByVerificationToken(token);
        HttpHeaders headers = new HttpHeaders();

        if (userOpt.isEmpty()) {
            headers.add("Location", frontendUrl + "/login?error=invalid-link");
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
        }

        User user = userOpt.get();
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userService.saveUser(user);

        headers.add("Location", frontendUrl + "/login?verified=true");
        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        String inputEmail    = loginRequest.getEmail()    == null ? "" : loginRequest.getEmail().trim().toLowerCase();
        String inputPassword = loginRequest.getPassword() == null ? "" : loginRequest.getPassword();

        Optional<User> existingUser = userService.findByEmail(inputEmail);

        if (existingUser.isPresent() && userService.checkPassword(inputPassword, existingUser.get().getPassword())) {
            if (!existingUser.get().isEmailVerified()) {
                return ResponseEntity.status(403)
                    .body(Map.of("message", "Please verify your email before logging in."));
            }

            String token = jwtUtil.generateToken(inputEmail, existingUser.get().getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("id", existingUser.get().getId());
            response.put("name", existingUser.get().getName());
            response.put("email", existingUser.get().getEmail());
            response.put("role", existingUser.get().getRole());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401)
            .body(Map.of("message", "Invalid credentials"));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklist.blacklist(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }
    

    // Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String token = userService.generateResetToken(email);

        // Always return same message (don't reveal if email exists)
        if (token != null) {
            emailService.sendPasswordResetEmail(email, token);
        }

        return ResponseEntity.ok(Map.of("message",
            "If that email exists, a reset link has been sent."));
    }

    // Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token    = request.get("token");
        String password = request.get("password");

        if (token == null || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Invalid request."));
        }

        boolean success = userService.resetPassword(token, password);

        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password reset successful! You can now login."));
        }

        return ResponseEntity.badRequest()
            .body(Map.of("message", "Invalid or expired reset link."));
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