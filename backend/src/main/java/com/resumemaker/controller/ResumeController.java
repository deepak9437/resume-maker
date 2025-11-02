package com.resumemaker.controller;

import com.resumemaker.model.Resume;
import com.resumemaker.service.ResumeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing resume operations.
 */
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * Create a new resume.
     *
     * @param resume the resume to create
     * @return the created resume with its ID
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody @NonNull Resume resume) {
        try {
            Resume saved = resumeService.save(resume);
            return ResponseEntity.ok(Map.of(
                "status", "ok",
                "resumeId", saved.getId()
            ));
        } catch (Exception ex) {
            logger.error("Failed to create resume", ex);
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Get a resume by its ID.
     *
     * @param id the ID of the resume
     * @return the resume if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResume(@NonNull @PathVariable String id) {
        return resumeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all resumes for a user with pagination.
     *
     * @param userId the ID of the user
     * @param page the page number (0-based)
     * @param size the size of each page
     * @return paginated list of resumes
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Resume>> getUserResumes(
            @NonNull @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(resumeService.findByUserId(userId, page, size));
    }

    /**
     * Search resumes by name.
     *
     * @param query the search query
     * @param page the page number (0-based)
     * @param size the size of each page
     * @return paginated list of matching resumes
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Resume>> searchResumes(
            @RequestParam @NonNull String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(resumeService.searchByName(query, page, size));
    }

    /**
     * Export a resume as PDF.
     *
     * @param id the ID of the resume to export
     * @return the PDF file
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<?> exportPdf(@NonNull @PathVariable String id) {
        try {
            Optional<Resume> resumeOpt = resumeService.findById(id);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Resume resume = resumeOpt.get();
            // Validate that the resume is not null before generating PDF
            if (resume == null) {
                logger.error("Resume is null for ID: {}", id);
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Invalid resume data"));
            }

            byte[] pdf = resumeService.generatePdf(resume);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"resume-%s.pdf\"", id));
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("Failed to generate PDF for resume {}", id, ex);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to generate PDF"));
        }
    }

    /**
     * Delete a resume.
     *
     * @param id the ID of the resume to delete
     * @return no content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(@NonNull @PathVariable String id) {
        try {
            if (!resumeService.exists(id)) {
                return ResponseEntity.notFound().build();
            }
            resumeService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            logger.error("Failed to delete resume {}", id, ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}