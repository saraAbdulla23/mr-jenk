package com.user_service.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    private static final String OTP_SUBJECT = "Your OTP Code";
    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Send an OTP email to the user.
     * Handles exceptions and logs errors if sending fails.
     */
    public void sendOtp(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(OTP_SUBJECT);
            message.setText(buildOtpMessage(otp));

            mailSender.send(message);
            logger.info("OTP email sent successfully to {}", toEmail);

        } catch (MailException e) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            // Optionally, you could throw a custom exception here if needed
        }
    }

    /** Build the OTP email body */
    private String buildOtpMessage(String otp) {
        return new StringBuilder()
                .append("Hello,\n\n")
                .append("Your One-Time Password (OTP) is: ").append(otp).append("\n")
                .append("This OTP will expire in ").append(OTP_EXPIRY_MINUTES).append(" minutes.\n\n")
                .append("If you did not request this, please ignore this email.\n\n")
                .append("Thank you,\n")
                .append("The NeoFlix Team")
                .toString();
    }
}