package vn.tt.practice.warrantyservice.exception;

public class WarrantyNotFoundException extends RuntimeException {
    public WarrantyNotFoundException(String message) {
        super(message);
    }
}
