package com.resumemaker.service;

import com.resumemaker.model.Resume;
import com.resumemaker.repository.ResumeRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public Resume save(Resume r) {
        return resumeRepository.save(r);
    }

    public Optional<Resume> findById(String id) {
        return resumeRepository.findById(id);
    }

    public byte[] generatePdf(Resume r) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float width = page.getMediaBox().getWidth() - 2 * margin;
                float yPosition = yStart;

                // Optional image (right side)
                if (r.getImageBase64() != null && !r.getImageBase64().isEmpty()) {
                    try {
                        byte[] imgBytes = Base64.getDecoder().decode(r.getImageBase64());
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imgBytes, "profile");
                        cs.drawImage(pdImage, 440, yStart - 100, 120, 120);
                    } catch (Exception ex) {
                        System.err.println("Failed to add image to PDF: " + ex.getMessage());
                    }
                }

                // Header - Name
                cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
                cs.beginText();
                cs.newLineAtOffset(margin, yPosition);
                cs.showText(r.getFullName() != null ? r.getFullName() : "");
                cs.endText();
                yPosition -= 25;

                // Contact line
                cs.setFont(PDType1Font.HELVETICA, 11);
                String contact = (r.getEmail() != null ? r.getEmail() : "") + "  |  " + (r.getMobile() != null ? r.getMobile() : "");
                cs.beginText();
                cs.newLineAtOffset(margin, yPosition);
                cs.showText(contact);
                cs.endText();
                yPosition -= 30;

                // Add sections with text wrapping
                yPosition = addSection(cs, "Profile", r.getSummary(), margin, yPosition, width);
                yPosition = addSection(cs, "Skills", r.getSkills(), margin, yPosition, width);
                yPosition = addSection(cs, "Education", r.getEducation(), margin, yPosition, width);
                yPosition = addSection(cs, "Experience", r.getExperience(), margin, yPosition, width);
                addSection(cs, "Hobbies", r.getHobbies(), margin, yPosition, width);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // --- HELPER METHODS ---
    private float addSection(PDPageContentStream cs, String title, String text, float margin, float yPosition, float width) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return yPosition; // Skip empty sections
        }

        // Section Title
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.beginText();
        cs.newLineAtOffset(margin, yPosition);
        cs.showText(title);
        cs.endText();
        yPosition -= 20;

        // Section Content with wrapping
        cs.setFont(PDType1Font.HELVETICA, 11);
        List<String> lines = wrapText(text, PDType1Font.HELVETICA, 11, width);
        for (String line : lines) {
            cs.beginText();
            cs.newLineAtOffset(margin, yPosition);
            cs.showText(line);
            cs.endText();
            yPosition -= 15; // Line spacing
        }
        return yPosition - 10; // Space after section
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            float width = font.getStringWidth(currentLine + " " + word) / 1000 * fontSize;
            if (width > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        lines.add(currentLine.toString());
        return lines;
    }
}