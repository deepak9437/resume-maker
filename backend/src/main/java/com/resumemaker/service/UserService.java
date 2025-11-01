// PLACEHOLDER: UserService
package com.resumemaker.service;

import com.resumemaker.model.User;
import com.resumemaker.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom; // *** NEW IMPORT ***
import java.time.Instant; // *** NEW IMPORT ***
import java.time.temporal.ChronoUnit; // *** NEW IMPORT ***
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {
    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private final SecureRandom random = new SecureRandom(); // *** NEW ***

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

    private boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    // *** NEW METHOD: Generate and save OTP ***
    public String generateOtp(String mobile) throws Exception {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new Exception("No user found with this mobile number."));

        // Generate 4-digit OTP
        String otp = String.format("%04d", random.nextInt(10000));
        
        // Set OTP and 10-minute expiry
        user.setOtp(otp);
        user.setOtpExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        userRepository.save(user);

        // In a real app, you'd send this via SMS and not return it.
        // We return it here for the demo.
        return otp;
    }

    // *** NEW METHOD: Reset password with OTP ***
    public void resetPassword(String mobile, String otp, String newPassword) throws Exception {
        User user = userRepository.findByMobile(mobile)
                .orElseThrow(() -> new Exception("Invalid mobile number."));

        // Check 1: OTP is correct
        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            throw new Exception("Invalid or incorrect OTP.");
        }

        // Check 2: OTP has not expired
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(Instant.now())) {
            throw new Exception("OTP has expired. Please request a new one.");
        }

        // All checks passed. Reset password and clear OTP.
        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }
}
