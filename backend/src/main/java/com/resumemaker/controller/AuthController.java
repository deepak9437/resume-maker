// PLACEHOLDER: AuthController
package com.resumemaker.controller;

import com.resumemaker.model.User;
import com.resumemaker.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
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

    // *** NEW ENDPOINT: Send OTP ***
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String mobile = body.get("mobile");
        if (mobile == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mobile number is required."));
        }
        try {
            String otp = userService.generateOtp(mobile);
            // In production, you DON'T return the OTP. This is just for our demo.
            return ResponseEntity.ok(Map.of("status", "ok", "message", "OTP sent.", "demoOtp", otp));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // *** NEW ENDPOINT: Reset Password ***
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String mobile = body.get("mobile");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");

        if (mobile == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields."));
        }

        try {
            userService.resetPassword(mobile, otp, newPassword);
            return ResponseEntity.ok(Map.of("status", "ok", "message", "Password reset successfully."));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
