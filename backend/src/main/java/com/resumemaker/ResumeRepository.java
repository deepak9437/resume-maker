// PLACEHOLDER: ResumeRepository
package com.resumemaker.repository;

import com.resumemaker.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

// This is the entire, correct file.
// It should be empty inside because Spring Data provides all the methods
// like findById(), save(), etc. automatically.
public interface ResumeRepository extends MongoRepository<Resume, String> {
    // There should be nothing in here!
}

