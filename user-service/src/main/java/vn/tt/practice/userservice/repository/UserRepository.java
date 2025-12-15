package vn.tt.practice.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.userservice.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}


