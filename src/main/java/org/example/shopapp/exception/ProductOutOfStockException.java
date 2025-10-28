package org.example.shopapp.exception;

public class ProductOutOfStockException extends RuntimeException {
    
    public ProductOutOfStockException(String message) {
        super(message);
    }
    
    public ProductOutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
