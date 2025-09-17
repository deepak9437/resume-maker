// PLACEHOLDER: ResumeRepository
package com.resumemaker.repository;

import com.resumemaker.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResumeRepository extends MongoRepository<Resume, String> {
    List<Resume> findByUserId(String userId);
}
