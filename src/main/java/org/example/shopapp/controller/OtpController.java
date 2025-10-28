package org.example.shopapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopapp.dto.request.OtpRequest;
import org.example.shopapp.dto.request.OtpVerificationRequest;
import org.example.shopapp.dto.response.ApiResponse;
import org.example.shopapp.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OtpController {
    
    private final OtpService otpService;
    
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<String>> sendOtp(@Valid @RequestBody OtpRequest request) {
        try {
            String message = otpService.generateOtp(request);
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request);
            return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", isValid));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
