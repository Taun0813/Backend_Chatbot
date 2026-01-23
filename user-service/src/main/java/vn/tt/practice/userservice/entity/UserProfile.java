package vn.tt.practice.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;

    private String phone;
    
    private String address;
    
    private String city;
    
    private String country;
    
    @Column(name = "postal_code")
    private String postalCode;
}
