package com.personal.ai_sqbs.service.impl;

import com.personal.ai_sqbs.config.MailProperties;
import com.personal.ai_sqbs.config.OtpProperties;
import com.personal.ai_sqbs.exception.AppException;
import com.personal.ai_sqbs.exception.ErrorCode;
import com.personal.ai_sqbs.service.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final OtpProperties otpProperties;

    // Sends the email verification OTP using configured mail sender details.
    @Override
    public void sendEmailVerificationOtp(String toEmail, String fullName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

            helper.setFrom(mailProperties.from(), mailProperties.senderName());
            helper.setTo(toEmail);
            helper.setSubject("Verify your AI Smart Queue account");
            helper.setText(buildEmailBody(fullName, otp), false);

            mailSender.send(message);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.MAIL_SEND_FAILED);
        }
    }

    // Builds the plain-text verification email body.
    private String buildEmailBody(String fullName, String otp) {
        return """
                Hello %s,

                Your AI Smart Queue email verification OTP is: %s

                This code expires in %d minutes. Do not share it with anyone.
                If you did not create this account, you can ignore this email.

                AI Smart Queue
                """.formatted(fullName, otp, otpProperties.emailVerificationExpirationMinutes());
    }
}
