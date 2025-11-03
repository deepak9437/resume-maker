package com.resumemaker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    private static final String FAST2SMS_ENDPOINT = "https://www.fast2sms.com/dev/bulkV2";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public SmsService(@Value("${fast2sms.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends an SMS using the Fast2SMS V2 API.
     * @param toMobileNumber The user's 10-digit mobile number.
     * @param body The text message (e.g., "Your OTP is 1234").
     */
    public void sendSms(String toMobileNumber, String body) {
        
        // Ensure it's a 10-digit number (remove +91 if present)
        if (toMobileNumber.startsWith("+91") && toMobileNumber.length() == 13) {
            toMobileNumber = toMobileNumber.substring(3);
        }

        // Fast2SMS requires a 10-digit number
        if (toMobileNumber.length() != 10) {
            logger.warn("Invalid mobile number format for Fast2SMS: {}. Must be 10 digits.", toMobileNumber);
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", this.apiKey);

        // Construct the request body as per Fast2SMS v2 docs
        Map<String, Object> requestBody = Map.of(
            "route", "q",         // "q" is their new transactional/OTP route
            "message", body,
            "language", "english",
            "sender_id", "FSTSMS", // Use default "FSTSMS" or your approved Sender ID
            "numbers", toMobileNumber  // Just the 10-digit number
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Send the request
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(FAST2SMS_ENDPOINT, entity, Map.class);
            logger.info("Fast2SMS API Response: {}", response);

            if (response != null && Boolean.TRUE.equals(response.get("return"))) {
                logger.info("SMS sent successfully via Fast2SMS to {}", toMobileNumber);
            } else {
                logger.error("Failed to send SMS via Fast2SMS: {}", response != null ? response.get("message") : "Unknown error");
            }
        } catch (Exception e) {
            logger.error("Error calling Fast2SMS API: {}", e.getMessage());
        }
    }
}


