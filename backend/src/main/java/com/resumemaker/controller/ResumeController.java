// ResumeController.java
package com.resumemaker.controller;

import com.resumemaker.model.Resume;
import com.resumemaker.model.ResumeData; // <-- import the new model
import com.resumemaker.service.ResumeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
public class ResumeController {
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // Save resume in DB
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Resume r) {
        try {
            Resume saved = resumeService.save(r);
            return ResponseEntity.ok(Map.of("status", "ok", "resumeId", saved.getId()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // Fetch resume by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getResume(@PathVariable String id) {
        Optional<Resume> r = resumeService.findById(id);
        if (r.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r.get());
    }

    // Export resume as PDF
    @GetMapping("/{id}/export")
    public ResponseEntity<?> exportPdf(@PathVariable String id) {
        try {
            Optional<Resume> r = resumeService.findById(id);
            if (r.isEmpty()) return ResponseEntity.notFound().build();
            byte[] pdf = resumeService.generatePdf(r.get());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume-" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to generate PDF"));
        }
    }

    // ✅ New: Generate resume instantly from frontend form
    @PostMapping("/generate")
    public ResponseEntity<?> generateResume(@RequestBody ResumeData resumeData) {
        try {
            // Debug log
            System.out.println("Generating resume for: " + resumeData.getName());

            // You can enhance this to call your service for PDF/HTML generation
            String resumePreview = "Resume for: " + resumeData.getName() +
                    "\nEmail: " + resumeData.getEmail() +
                    "\nPhone: " + resumeData.getPhone() +
                    "\nSummary: " + resumeData.getSummary() +
                    "\nEducation: " + resumeData.getEducation() +
                    "\nExperience: " + resumeData.getExperience() +
                    "\nSkills: " + resumeData.getSkills();

            // For now just return text preview to confirm data is flowing
            return ResponseEntity.ok(Map.of("status", "ok", "resumeContent", resumePreview));

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
