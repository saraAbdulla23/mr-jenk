package com.user_service.service;

import com.user_service.model.Otp;
import com.user_service.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;

    /** Generate OTP */
    @Transactional
    public String generateOtp(String email) {
        // Remove existing OTP if any
        otpRepository.deleteByEmail(email);

        // Generate new 6-digit OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        Otp otpEntity = new Otp(email, otp, LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);

        return otp;
    }

    /** Verify OTP */
    @Transactional
    public void verifyOtp(String email, String code) {
        Otp otp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (otp.getExpiry().isBefore(LocalDateTime.now())) {
            otpRepository.deleteByEmail(email);
            throw new RuntimeException("OTP expired");
        }

        if (!otp.getCode().equals(code)) {
            throw new RuntimeException("Invalid OTP");
        }

        otpRepository.deleteByEmail(email);
    }
}