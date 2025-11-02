package com.resumemaker.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "resumes")
public class Resume {
    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String templateId;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid mobile number")
    private String mobile;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @Size(max = 500, message = "Summary cannot exceed 500 characters")
    private String summary;
    // skills stored as comma-separated string for simplicity
    @Size(max = 500, message = "Skills cannot exceed 500 characters")
    private String skills;

    @Size(max = 1000, message = "Education details cannot exceed 1000 characters")
    private String education;

    @Size(max = 2000, message = "Experience details cannot exceed 2000 characters")
    private String experience;

    @Size(max = 200, message = "Hobbies cannot exceed 200 characters")
    private String hobbies;

    // Profile image stored as Base64 string (without data URI prefix)
    private String imageBase64;

    public Resume() {}

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
