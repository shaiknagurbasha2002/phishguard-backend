package com.phishguard.controller;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/api/test")
	public ResponseEntity<?> test() {
	    return ResponseEntity.ok(Map.of("message", "You are authenticated!"));
	}
}