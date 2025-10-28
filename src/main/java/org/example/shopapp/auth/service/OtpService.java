package org.example.shopapp.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopapp.auth.dto.request.OtpRequest;
import org.example.shopapp.auth.dto.request.OtpVerificationRequest;
import org.example.shopapp.common.entity.Otp;
import org.example.shopapp.common.exception.InvalidOtpException;
import org.example.shopapp.auth.repository.OtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    
    private final OtpRepository otpRepository;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final int MAX_OTP_REQUESTS_PER_HOUR = 5;
    
    @Transactional
    public String generateOtp(OtpRequest request) {
        String phoneNumber = request.getPhoneNumber();
        
        // Check rate limiting
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentOtps = otpRepository.countByPhoneNumberSince(phoneNumber, oneHourAgo);
        
        if (recentOtps >= MAX_OTP_REQUESTS_PER_HOUR) {
            throw new RuntimeException("Too many OTP requests. Please try again later.");
        }
        
        // Generate OTP
        String otpCode = generateRandomOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        
        // Save OTP
        Otp otp = Otp.builder()
                .phoneNumber(phoneNumber)
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .isUsed(false)
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();
        
        otpRepository.save(otp);
        
        // In a real application, you would send SMS here
        log.info("OTP for {}: {}", phoneNumber, otpCode);
        
        return "OTP sent successfully to " + phoneNumber;
    }
    
    @Transactional
    public boolean verifyOtp(OtpVerificationRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String otpCode = request.getOtpCode();
        
        Otp otp = otpRepository.findByPhoneNumberAndOtpCodeAndIsUsedFalse(phoneNumber, otpCode)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP code"));
        
        // Check if OTP is expired
        if (otp.isExpired()) {
            otp.incrementAttempts();
            otpRepository.save(otp);
            throw new InvalidOtpException("OTP has expired");
        }
        
        // Check if too many attempts
        if (otp.getAttempts() >= MAX_OTP_ATTEMPTS) {
            throw new InvalidOtpException("Too many failed attempts. Please request a new OTP.");
        }
        
        // Mark OTP as used
        otp.markAsUsed();
        otpRepository.save(otp);
        
        return true;
    }
    
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
    
    private String generateRandomOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
}
