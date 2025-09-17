// PLACEHOLDER: AuthController
package com.resumemaker.controller;

import com.resumemaker.model.User;
import com.resumemaker.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String fullName = body.get("fullName");
            String email = body.get("email");
            String mobile = body.get("mobile");
            String userId = body.get("userId");
            String password = body.get("password");
            if (fullName==null || email==null || mobile==null || userId==null || password==null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
            }
            User u = new User();
            u.setFullName(fullName);
            u.setEmail(email);
            u.setMobile(mobile);
            u.setUserId(userId);
            User saved = userService.register(u, password);
            return ResponseEntity.ok(Map.of("status", "ok", "userId", saved.getId()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing credentials"));
        }
        return userService.login(username, password)
                .map(u -> ResponseEntity.ok(Map.of("status", "ok", "userId", u.getId(), "userName", u.getFullName())))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }
}
