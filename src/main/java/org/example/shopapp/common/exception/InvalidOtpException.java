package org.example.shopapp.common.exception;

public class InvalidOtpException extends RuntimeException {
    
    public InvalidOtpException(String message) {
        super(message);
    }
    
    public InvalidOtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
