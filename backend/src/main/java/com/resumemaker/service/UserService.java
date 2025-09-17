// PLACEHOLDER: UserService
package com.resumemaker.service;

import com.resumemaker.model.User;
import com.resumemaker.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

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
}
