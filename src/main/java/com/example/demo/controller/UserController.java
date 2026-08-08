package com.example.demo.controller;

import com.example.demo.dto.AddressDto;
import com.example.demo.dto.MessageResponse;
import com.example.demo.dto.UserDto;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Integer getUserId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(getUserId(authentication)));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(Authentication authentication, @RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.updateProfile(getUserId(authentication), dto));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<List<AddressDto>> getAddresses(Authentication authentication) {
        return ResponseEntity.ok(userService.getAddresses(getUserId(authentication)));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<AddressDto> addAddress(Authentication authentication, @RequestBody AddressDto dto) {
        return ResponseEntity.ok(userService.addAddress(getUserId(authentication), dto));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<MessageResponse> deleteAddress(Authentication authentication, @PathVariable Long addressId) {
        userService.deleteAddress(getUserId(authentication), addressId);
        return ResponseEntity.ok(new MessageResponse("Address deleted"));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<MessageResponse> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(getUserId(authentication), request));
    }
}
