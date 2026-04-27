package com.group1.career.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendVerificationCode(String toEmail, String code, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);

            String subject;
            String htmlContent;

            if ("REGISTER".equals(purpose)) {
                subject = "【Career Loop】邮箱验证码";
                htmlContent = buildVerificationEmail(code, "注册账号", 5);
            } else {
                subject = "【Career Loop】密码重置验证码";
                htmlContent = buildVerificationEmail(code, "重置密码", 5);
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }

    private String buildVerificationEmail(String code, String action, int expireMinutes) {
        return """
                <div style="font-family:-apple-system,BlinkMacSystemFont,'Helvetica Neue',Arial,sans-serif;
                            max-width:480px;margin:0 auto;padding:32px 24px;background:#ffffff;">
                  <div style="text-align:center;margin-bottom:24px;">
                    <span style="font-size:22px;font-weight:800;color:#2457d6;letter-spacing:1px;">CAREER LOOP</span>
                  </div>
                  <h2 style="font-size:18px;font-weight:700;color:#1e293b;margin-bottom:8px;">验证码 — %s</h2>
                  <p style="color:#64748b;font-size:14px;margin-bottom:24px;">
                    您正在 Career Loop 进行 <strong>%s</strong>，请使用以下验证码完成操作：
                  </p>
                  <div style="background:#eef4ff;border-radius:14px;padding:20px;text-align:center;margin-bottom:24px;">
                    <span style="font-size:36px;font-weight:800;letter-spacing:10px;color:#2457d6;">%s</span>
                  </div>
                  <p style="color:#94a3b8;font-size:12px;margin:0;">
                    验证码 <strong>%d 分钟</strong>内有效，请勿泄露给他人。<br/>
                    如非本人操作，请忽略此邮件。
                  </p>
                </div>
                """.formatted(action, action, code, expireMinutes);
    }
}
