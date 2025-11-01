package com.resumemaker.repository;

import com.resumemaker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
// import org.springframework.stereotype.Repository; // <-- Removed this line

import java.util.Optional;

// Note: The @Repository annotation was removed. 
// It is redundant for Spring Data repositories and causes a warning.
public interface UserRepository extends MongoRepository<User, String> {

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find user by mobile number
    Optional<User> findByMobile(String mobile);

    // Find user by userId (custom field, not Mongo _id)
    Optional<User> findByUserId(String userId);

    // Convenience method for login (can match email OR mobile OR userId)
    Optional<User> findByEmailOrMobileOrUserId(String email, String mobile, String userId);
}
