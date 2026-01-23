package vn.tt.practice.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tt.practice.userservice.model.Address;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
}
