package com.elice.aurasphere.user.service;

import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.entity.EmailVerification;
import com.elice.aurasphere.user.repository.EmailVerificationRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.AssertingParty.Verification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final EmailVerificationRepository verificationRepository;
    private final UserService userService;

    public void sendEmail(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("회원가입 이메일 인증");
        message.setText("인증 코드: " + verificationCode + "\n\n이 코드는 30분 동안 유효합니다.");
        emailSender.send(message);
    }

    @Transactional
    public void createAndSendVerification(String email) {
        // 기존 인증 정보가 있다면 삭제
        verificationRepository.findByEmailWithPessimisticLock(email)
            .ifPresent(verificationRepository::delete);

        // 이메일 중복 체크
        if (userService.checkEmailDuplication(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String verificationCode = generateVerificationCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

        // 새로운 인증 정보 저장
        EmailVerification verification = new EmailVerification(email, verificationCode, expiryDate);
        verificationRepository.save(verification);

        // 이메일 발송
        sendEmail(email, verificationCode);
    }

    public void  verifyEmail(String email, String code) {
        EmailVerification verification = verificationRepository
            .findByEmailAndVerificationCode(email, code)
            .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        if (verification.isExpired()) {
            throw new CustomException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        if (verification.isVerified()) {
            throw new CustomException(ErrorCode.VERIFICATION_ALREADY_VERIFIED);
        }

        verification.verify();
        verificationRepository.save(verification);
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // 주기적으로 만료된 인증 정보 삭제 (스케줄링)
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanUpExpiredVerifications() {
        verificationRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    public boolean isEmailVerified(String email) {
        return verificationRepository.findByEmail(email)
            .map(EmailVerification::isVerified)
            .orElse(false);
    }

}
