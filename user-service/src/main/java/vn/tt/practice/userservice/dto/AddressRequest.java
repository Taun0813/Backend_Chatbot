package vn.tt.practice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {
    private String fullName;
    private String phone;
    private String addressLine;
    private String city;
    private String district;
    private String ward;
    private Boolean isDefault;
}
