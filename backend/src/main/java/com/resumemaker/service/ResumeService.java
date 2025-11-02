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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);
    private static final float MARGIN = 50;
    private static final float IMAGE_WIDTH = 120;
    private static final float IMAGE_HEIGHT = 120;
    
    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public Resume save(@NonNull Resume resume) {
        return resumeRepository.save(resume);
    }

    public Optional<Resume> findById(@NonNull String id) {
        return resumeRepository.findById(id);
    }

    public byte[] generatePdf(@NonNull Resume resume) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float yPosition = generatePdfContent(doc, page, resume);
            
            // If content exceeds page, log warning (TODO: implement pagination)
            if (yPosition < MARGIN) {
                logger.warn("Content exceeded page boundaries");
            }

            doc.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Failed to generate PDF", e);
            throw e;
        }
    }

    private float generatePdfContent(PDDocument doc, PDPage page, Resume resume) throws IOException {
        PDFont helvetica = PDType1Font.HELVETICA;
        PDFont helveticaBold = PDType1Font.HELVETICA_BOLD;
        float yStart = page.getMediaBox().getHeight() - MARGIN;
        float width = page.getMediaBox().getWidth() - 2 * MARGIN;
        float yPosition = yStart;

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            // Add profile image if available
            yPosition = addProfileImage(doc, cs, resume.getImageBase64(), yStart);

            // Add resume sections
            yPosition = addHeader(cs, resume, helvetica, helveticaBold, yPosition);
            yPosition = addSection(cs, "Profile", resume.getSummary(), MARGIN, yPosition, width, helvetica, helveticaBold);
            yPosition = addSection(cs, "Skills", resume.getSkills(), MARGIN, yPosition, width, helvetica, helveticaBold);
            yPosition = addSection(cs, "Education", resume.getEducation(), MARGIN, yPosition, width, helvetica, helveticaBold);
            yPosition = addSection(cs, "Experience", resume.getExperience(), MARGIN, yPosition, width, helvetica, helveticaBold);
            yPosition = addSection(cs, "Hobbies", resume.getHobbies(), MARGIN, yPosition, width, helvetica, helveticaBold);
        }
        return yPosition;
    }

    private float addProfileImage(PDDocument doc, PDPageContentStream cs, String imageBase64, float yStart) throws IOException {
        if (imageBase64 != null && !imageBase64.trim().isEmpty()) {
            try {
                byte[] imgBytes = Base64.getDecoder().decode(imageBase64);
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imgBytes, "profile");
                cs.drawImage(pdImage, 440, yStart - IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid image data", e);
            } catch (IOException e) {
                logger.error("Failed to process image", e);
            }
        }
        return yStart;
    }

    private float addHeader(PDPageContentStream cs, Resume resume, PDFont font, PDFont boldFont, float yPosition) throws IOException {
        // Name
        cs.setFont(boldFont, 20);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, yPosition);
        cs.showText(resume.getFullName() != null ? resume.getFullName() : "");
        cs.endText();
        yPosition -= 25;

        // Contact information
        cs.setFont(font, 11);
        String contact = String.format("%s  |  %s",
                resume.getEmail() != null ? resume.getEmail() : "",
                resume.getMobile() != null ? resume.getMobile() : "");
        cs.beginText();
        cs.newLineAtOffset(MARGIN, yPosition);
        cs.showText(contact);
        cs.endText();
        
        return yPosition - 30;
    }

    private float addSection(PDPageContentStream cs, String title, String text, float margin, float yPosition, float width, PDFont font, PDFont fontBold) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return yPosition;
        }

        // Section Title
        cs.setFont(fontBold, 12);
        cs.beginText();
        cs.newLineAtOffset(margin, yPosition);
        cs.showText(title);
        cs.endText();
        yPosition -= 20;

        // Section Content
        cs.setFont(font, 11);
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            List<String> lines = wrapText(paragraph, font, 11, width);
            for (String line : lines) {
                if (yPosition < MARGIN) {
                    logger.warn("Text overflow in section: {}", title);
                    return yPosition;
                }
                cs.beginText();
                cs.newLineAtOffset(margin, yPosition);
                cs.showText(line);
                cs.endText();
                yPosition -= 15;
            }
        }
        return yPosition - 10;
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return lines;
        
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String lineWithWord = currentLine.length() > 0 
                ? currentLine + " " + word 
                : word;
            
            float width = font.getStringWidth(lineWithWord) / 1000 * fontSize;
            
            if (width > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
}