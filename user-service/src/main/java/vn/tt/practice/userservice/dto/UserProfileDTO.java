package vn.tt.practice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String postalCode;
}
