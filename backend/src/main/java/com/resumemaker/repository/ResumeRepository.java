package com.resumemaker.repository;

import com.resumemaker.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Repository interface for managing Resume documents in MongoDB.
 * Extends MongoRepository to provide CRUD operations and custom queries for Resume entities.
 */
public interface ResumeRepository extends MongoRepository<Resume, String> {
    
    /**
     * Find all resumes belonging to a specific user with pagination support.
     *
     * @param userId The ID of the user whose resumes to find
     * @param pageable Pagination information
     * @return A Page of Resume objects
     */
    Page<Resume> findByUserId(@NonNull String userId, Pageable pageable);

    /**
     * Find all resumes belonging to a specific user.
     *
     * @param userId The ID of the user whose resumes to find
     * @return List of Resume objects
     */
    List<Resume> findByUserId(@NonNull String userId);

    /**
     * Find resumes by matching full name (case-insensitive).
     *
     * @param fullName The full name to search for
     * @param pageable Pagination information
     * @return A Page of Resume objects
     */
    @Query("{'fullName': {$regex: ?0, $options: 'i'}}")
    Page<Resume> findByFullNameContainingIgnoreCase(@NonNull String fullName, Pageable pageable);

    /**
     * Count the number of resumes for a specific user.
     *
     * @param userId The ID of the user
     * @return The number of resumes owned by the user
     */
    long countByUserId(@NonNull String userId);

    /**
     * Delete all resumes belonging to a specific user.
     *
     * @param userId The ID of the user whose resumes should be deleted
     */
    void deleteByUserId(@NonNull String userId);
}
