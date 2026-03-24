package vn.tt.practice.warrantyservice.exception;

public class WarrantyExpiredException extends RuntimeException {
    public WarrantyExpiredException(String message) {
        super(message);
    }
}
