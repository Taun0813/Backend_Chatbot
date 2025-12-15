package vn.tt.practice.userservice.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    private String fullName;
    private String phone;
}

