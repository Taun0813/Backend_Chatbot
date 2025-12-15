package vn.tt.practice.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    @NotBlank
    private String addressLine;

    private String city;
    private String district;
    private String ward;

    private Boolean isDefault;
}

