package com.elice.aurasphere.user.repository;

import com.elice.aurasphere.user.entity.EmailVerification;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmailAndVerificationCode(String email, String code);
    Optional<EmailVerification> findByEmail(String email);
    void deleteByExpiryDateBefore(LocalDateTime date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM EmailVerification v WHERE v.email = :email")
    Optional<EmailVerification> findByEmailWithPessimisticLock(@Param("email") String email);
}
