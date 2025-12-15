package vn.tt.practice.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.userservice.model.Address;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserId(UUID userId);
}

