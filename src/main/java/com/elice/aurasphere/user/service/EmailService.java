package com.elice.aurasphere.user.service;

import com.elice.aurasphere.global.exception.CustomException;
import com.elice.aurasphere.global.exception.ErrorCode;
import com.elice.aurasphere.user.entity.EmailVerification;
import com.elice.aurasphere.user.repository.EmailVerificationRepository;
import com.elice.aurasphere.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final UserService userService;

    public void sendEmail(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("회원가입 이메일 인증");
        message.setText("인증 코드: " + verificationCode + "\n\n이 코드는 30분 동안 유효합니다.");
        emailSender.send(message);
    }

    public void sendVerificationEmail(String toEmail, String verificationCode) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("회원가입 이메일 인증 코드");
        mailMessage.setText("회원가입을 위한 인증 코드: " + verificationCode +
            "\n이 코드는 10분 동안 유효합니다.");
        emailSender.send(mailMessage);
    }

    public void sendPasswordResetEmail(String toEmail, String verificationCode) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("비밀번호 재설정 인증 코드");
        mailMessage.setText("비밀번호 재설정을 위한 인증 코드: " + verificationCode +
            "\n이 코드는 10분 동안 유효합니다.");
        emailSender.send(mailMessage);
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
        sendVerificationEmail(email, verificationCode);
    }

    @Transactional
    public void createAndSendPasswordResetVerification(String email) {
        // 이메일이 DB에 존재하는지 확인
        if (!userRepository.findByEmail(email).isPresent()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 기존 인증 코드가 있다면 만료
        verificationRepository.findByEmail(email)
            .ifPresent(verification -> {
                if (!verification.isExpired()) {
                    verificationRepository.delete(verification);
                }
            });

        String verificationCode = generateVerificationCode();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

        // 새로운 인증 정보 저장
        EmailVerification verification = new EmailVerification(email, verificationCode, expiryDate);
        verificationRepository.save(verification);

        // 이메일 발송
        sendPasswordResetEmail(email, verificationCode);
    }

    public void verifyEmail(String email, String code) {
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
