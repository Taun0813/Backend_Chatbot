package vn.tt.practice.userservice.service;

import vn.tt.practice.userservice.dto.AddressRequest;
import vn.tt.practice.userservice.dto.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    List<AddressResponse> getUserAddresses(Long userId);
    AddressResponse addAddress(Long userId, AddressRequest request);
    AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);
    void deleteAddress(Long userId, Long addressId);
}
