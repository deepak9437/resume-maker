// PLACEHOLDER: ResumeService
package com.resumemaker.service;

import com.resumemaker.model.Resume;
import com.resumemaker.repository.ResumeRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
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
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(doc, page);

        // Header - Name
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
        cs.newLineAtOffset(50, 750);
        cs.showText(r.getFullName() == null ? "Full Name" : r.getFullName());
        cs.endText();

        // Contact line
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(50, 730);
        String contact = (r.getEmail() == null ? "" : r.getEmail()) + "  |  " + (r.getMobile() == null ? "" : r.getMobile());
        cs.showText(contact);
        cs.endText();

        // Summary
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(50, 700);
        cs.showText("Profile");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(50, 685);
        String summary = r.getSummary() == null ? "" : r.getSummary();
        // Keep summary short in PDF
        cs.showText(summary.length() > 200 ? summary.substring(0, 200) + "..." : summary);
        cs.endText();

        // Skills
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(50, 660);
        cs.showText("Skills");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(50, 645);
        cs.showText(r.getSkills() == null ? "" : r.getSkills());
        cs.endText();

        // Education (multi-line naive)
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(50, 620);
        cs.showText("Education");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(50, 605);
        String edu = r.getEducation() == null ? "" : r.getEducation();
        cs.showText(edu.length() > 300 ? edu.substring(0, 300) + "..." : edu);
        cs.endText();

        // Experience
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(50, 580);
        cs.showText("Experience");
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 11);
        cs.newLineAtOffset(50, 565);
        String exp = r.getExperience() == null ? "" : r.getExperience();
        cs.showText(exp.length() > 300 ? exp.substring(0, 300) + "..." : exp);
        cs.endText();

        // Optional image (right side)
        if (r.getImageBase64() != null && !r.getImageBase64().isEmpty()) {
            try {
                byte[] imgBytes = Base64.getDecoder().decode(r.getImageBase64());
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imgBytes, "profile");
                // draw image on top-right
                cs.drawImage(pdImage, 400, 640, 120, 120);
            } catch (Exception ex) {
                // ignore image errors for now
            }
        }

        cs.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        doc.close();
        return baos.toByteArray();
    }
}
