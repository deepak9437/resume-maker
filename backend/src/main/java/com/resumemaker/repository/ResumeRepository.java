// PLACEHOLDER: ResumeRepository
package com.resumemaker.repository;

import com.resumemaker.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends MongoRepository<Resume, String> {
    List<Resume> findByUserId(String userId);
    Optional<Resume> findById(String id);
}