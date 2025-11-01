// PLACEHOLDER: UserService
package com.resumemaker.service;

import com.resumemaker.model.User;
import com.resumemaker.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

// --- Imports for Password Reset ---
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {
    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(User u, String rawPassword) throws Exception {
        // basic uniqueness checks
        if (userRepository.findByEmail(u.getEmail()).isPresent()) {
            throw new Exception("Email already exists");
        }
        if (userRepository.findByMobile(u.getMobile()).isPresent()) {
            throw new Exception("Mobile already exists");
        }
        if (userRepository.findByUserId(u.getUserId()).isPresent()) {
            throw new Exception("UserId already exists");
        }
        if (!isEmailValid(u.getEmail())) {
            throw new Exception("Invalid email format");
        }
        String pwHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        u.setPasswordHash(pwHash);
        return userRepository.save(u);
    }

    public Optional<User> login(String usernameOrEmailOrMobile, String rawPassword) {
        Optional<User> userOpt =
                userRepository.findByEmailOrMobileOrUserId(usernameOrEmailOrMobile,
                                                          usernameOrEmailOrMobile,
                                                          usernameOrEmailOrMobile);
        if (userOpt.isEmpty()) return Optional.empty();
        User u = userOpt.get();
        if (BCrypt.checkpw(rawPassword, u.getPasswordHash())) {
            return Optional.of(u);
        } else {
            return Optional.empty();
        }
    }

    // --- THIS IS THE METHOD YOU WERE MISSING ---
    public String generateOtp(String mobile) throws Exception {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new Exception("User not found with that mobile number."));

        // Generate 4-digit OTP
        String otp = new SecureRandom().ints(1000, 10000).findFirst().getAsInt() + "";
        
        user.setOtp(otp);
        user.setOtpExpiry(Instant.now().plus(10, ChronoUnit.MINUTES)); // 10-minute expiry
        userRepository.save(user);
        
        return otp;
    }

    // --- THIS METHOD IS ALSO NEEDED ---
    public void resetPassword(String mobile, String otp, String newPassword) throws Exception {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new Exception("User not found."));

        // Check if OTP is valid and not expired
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new Exception("Invalid OTP.");
        }
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(Instant.now())) {
            throw new Exception("OTP has expired.");
        }

        // All checks passed. Reset password.
        String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        user.setPasswordHash(newPasswordHash);

        // Clear the OTP fields
        user.setOtp(null);
        user.setOtpExpiry(null);
        
        userRepository.save(user);
    }


    private boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}

