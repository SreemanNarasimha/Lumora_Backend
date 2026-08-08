package com.example.demo.repository;
import com.example.demo.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByRefreshTokenHash(String refreshTokenHash);
    List<JwtToken> findByUserUserIdAndRevokedFalse(Integer userId);
}
