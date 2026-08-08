package com.example.demo.controller;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressController(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private Integer getUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAddresses(Authentication authentication) {
        Integer userId = getUserId(authentication);
        return ResponseEntity.ok(addressRepository.findByUserUserId(userId));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Address> createAddress(Authentication authentication, @RequestBody Address addressRequest) {
        Integer userId = getUserId(authentication);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(addressRequest.getIsDefault())) {
            List<Address> existing = addressRepository.findByUserUserId(userId);
            for (Address a : existing) {
                a.setIsDefault(false);
                addressRepository.save(a);
            }
        }

        Address address = new Address();
        address.setUser(user);
        address.setLabel(addressRequest.getLabel() != null ? addressRequest.getLabel() : "Home");
        address.setLine1(addressRequest.getLine1());
        address.setLine2(addressRequest.getLine2());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setPostalCode(addressRequest.getPostalCode());
        address.setCountry(addressRequest.getCountry() != null ? addressRequest.getCountry() : "India");
        address.setPhone(addressRequest.getPhone());
        address.setIsDefault(Boolean.TRUE.equals(addressRequest.getIsDefault()));

        return ResponseEntity.ok(addressRepository.save(address));
    }

    @PutMapping("/{addressId}")
    @Transactional
    public ResponseEntity<Address> updateAddress(Authentication authentication,
                                                 @PathVariable Long addressId,
                                                 @RequestBody Address addressRequest) {
        Integer userId = getUserId(authentication);
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (Boolean.TRUE.equals(addressRequest.getIsDefault())) {
            List<Address> existing = addressRepository.findByUserUserId(userId);
            for (Address a : existing) {
                a.setIsDefault(false);
                addressRepository.save(a);
            }
        }

        if (addressRequest.getLabel() != null) address.setLabel(addressRequest.getLabel());
        if (addressRequest.getLine1() != null) address.setLine1(addressRequest.getLine1());
        if (addressRequest.getLine2() != null) address.setLine2(addressRequest.getLine2());
        if (addressRequest.getCity() != null) address.setCity(addressRequest.getCity());
        if (addressRequest.getState() != null) address.setState(addressRequest.getState());
        if (addressRequest.getPostalCode() != null) address.setPostalCode(addressRequest.getPostalCode());
        if (addressRequest.getCountry() != null) address.setCountry(addressRequest.getCountry());
        if (addressRequest.getPhone() != null) address.setPhone(addressRequest.getPhone());
        if (addressRequest.getIsDefault() != null) address.setIsDefault(addressRequest.getIsDefault());

        return ResponseEntity.ok(addressRepository.save(address));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<MessageResponse> deleteAddress(Authentication authentication, @PathVariable Long addressId) {
        Integer userId = getUserId(authentication);
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        addressRepository.delete(address);
        return ResponseEntity.ok(new MessageResponse("Address deleted successfully"));
    }

    @PutMapping("/{addressId}/default")
    @Transactional
    public ResponseEntity<MessageResponse> setDefaultAddress(Authentication authentication, @PathVariable Long addressId) {
        Integer userId = getUserId(authentication);
        List<Address> existing = addressRepository.findByUserUserId(userId);
        for (Address a : existing) {
            a.setIsDefault(a.getAddressId().equals(addressId));
            addressRepository.save(a);
        }
        return ResponseEntity.ok(new MessageResponse("Default address updated"));
    }
}
