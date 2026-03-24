package vn.tt.practice.userservice.entity;

public enum Role {
    ROLE_USER("User"),
    ROLE_ADMIN("Admin"),
    ROLE_SUPER_ADMIN("Super Admin");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
