// PLACEHOLDER: ResumeRepository
package com.resumemaker.repository;

import com.resumemaker.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

// No methods are needed here!
// Spring Data provides findById(), save(), findAll(), etc. automatically.
// The two methods you had were:
// 1. findById() - which is redundant.
// 2. findByUserId() - which was unused.
public interface ResumeRepository extends MongoRepository<Resume, String> {
    // This file is now clean!
}
