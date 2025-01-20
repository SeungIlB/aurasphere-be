package com.elice.aurasphere.user.service;

import com.elice.aurasphere.user.entity.EmailVerification;
import com.elice.aurasphere.user.repository.EmailVerificationRepository;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final EmailVerificationRepository verificationRepository;

    public void sendEmail(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("회원가입 이메일 인증");
        message.setText("인증 코드: " + verificationCode + "\n\n이 코드는 30분 동안 유효합니다.");
        emailSender.send(message);
    }

    public void createAndSendVerification(String email) {
        String verificationCode = generateVerificationCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

        // 기존 인증 정보가 있다면 삭제
        verificationRepository.findByEmail(email)
            .ifPresent(verificationRepository::delete);

        // 새로운 인증 정보 저장
        EmailVerification verification = new EmailVerification(email, verificationCode, expiryDate);
        verificationRepository.save(verification);

        // 이메일 발송
        sendEmail(email, verificationCode);
    }

    public boolean verifyEmail(String email, String code) {
        return verificationRepository.findByEmailAndVerificationCode(email, code)
            .filter(verification -> !verification.isExpired())
            .map(verification -> {
                verification.verify();
                return true;
            })
            .orElse(false);
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
