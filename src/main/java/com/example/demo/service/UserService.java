package com.example.demo.service;

import com.example.demo.dto.AddressDto;
import com.example.demo.dto.UserDto;
import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.MessageResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.repository.OrderRepository;
import com.example.demo.entity.Order;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    public UserService(UserRepository userRepository, AddressRepository addressRepository, PasswordEncoder passwordEncoder, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
    }

    public UserDto getProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapUserToDto(user);
    }

    @Transactional
    public UserDto updateProfile(Integer userId, UserDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        return mapUserToDto(userRepository.save(user));
    }

    public List<AddressDto> getAddresses(Integer userId) {
        return addressRepository.findByUserUserId(userId).stream()
                .map(this::mapAddressToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDto addAddress(Integer userId, AddressDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            List<Address> existing = addressRepository.findByUserUserId(userId);
            existing.forEach(a -> a.setIsDefault(false));
            addressRepository.saveAll(existing);
        }

        Address address = new Address();
        address.setUser(user);
        address.setLabel(dto.getLabel());
        address.setLine1(dto.getLine1());
        address.setLine2(dto.getLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry() != null ? dto.getCountry() : "India");
        address.setPhone(dto.getPhone());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);

        return mapAddressToDto(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Integer userId, Long addressId) {
        Address address = addressRepository.findByAddressIdAndUserUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        addressRepository.delete(address);
    }

    @Transactional
    public MessageResponse changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new MessageResponse("Password updated successfully");
    }

    private UserDto mapUserToDto(User user) {
        return new UserDto(user.getUserId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.getLoyaltyPoints());
    }

    private AddressDto mapAddressToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setAddressId(address.getAddressId());
        dto.setLabel(address.getLabel());
        dto.setLine1(address.getLine1());
        dto.setLine2(address.getLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setPhone(address.getPhone());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }

    @Transactional
    public void deleteAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // GDPR: Anonymize orders
        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        for (Order o : orders) {
            o.setUser(null);
            o.setUserEmail("deleted_user_" + userId + "@lumora.com");
        }
        orderRepository.saveAll(orders);

        // Delete user (cascade removes addresses usually, or delete manually if needed)
        addressRepository.deleteAll(addressRepository.findByUserUserId(userId));
        userRepository.delete(user);
    }
}
