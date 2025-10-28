package org.example.shopapp.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopapp.auth.dto.request.OtpRequest;
import org.example.shopapp.auth.dto.request.OtpVerificationRequest;
import org.example.shopapp.common.dto.response.ApiResponse;
import org.example.shopapp.auth.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "OTP Authentication", description = "OTP-based authentication endpoints")
public class OtpController {
    
    private final OtpService otpService;
    
    @Operation(summary = "Send OTP", description = "Sends an OTP to the provided phone number for authentication")
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
    
    @Operation(summary = "Verify OTP", description = "Verifies the provided OTP code for authentication")
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
