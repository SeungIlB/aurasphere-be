package com.elice.aurasphere.user.repository;

import com.elice.aurasphere.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUsername(String username);
    void deleteByUsername(String username);
}