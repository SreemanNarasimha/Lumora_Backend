package com.example.demo.repository;

import com.example.demo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserUserId(Integer userId);
    Optional<Address> findByAddressIdAndUserUserId(Long addressId, Integer userId);
}
