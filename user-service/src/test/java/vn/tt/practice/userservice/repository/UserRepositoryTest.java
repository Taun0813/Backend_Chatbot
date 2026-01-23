package vn.tt.practice.userservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import vn.tt.practice.userservice.model.User;
import vn.tt.practice.userservice.model.UserRole;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_Success() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.ROLE_USER)
                .build();
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("testuser");

        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    void existsByEmail_True() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.ROLE_USER)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertTrue(exists);
    }
}
