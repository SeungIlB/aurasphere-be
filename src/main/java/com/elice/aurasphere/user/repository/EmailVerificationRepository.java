package com.elice.aurasphere.user.repository;

import com.elice.aurasphere.user.entity.EmailVerification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmailAndVerificationCode(String email, String code);
    Optional<EmailVerification> findByEmail(String email);
    void deleteByExpiryDateBefore(LocalDateTime date);
}
