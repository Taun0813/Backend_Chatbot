package vn.tt.practice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.tt.practice.userservice.entity.Role;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private UserProfileDTO profile;
}
