package com.phishguard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PhishGuard - Verify Your Email");

            String verifyLink = baseUrl + "/users/verify?token=" + token;

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2 style='color: #2c3e50;'>Welcome to PhishGuard! 🛡️</h2>"
                + "<p>Thank you for registering. Please verify your email by clicking the button below:</p>"
                + "<a href='" + verifyLink + "' style='background-color: #3498db; color: white; "
                + "padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;'>"
                + "Verify Email</a>"
                + "<p style='margin-top: 20px; color: #888;'>This link expires in 24 hours.</p>"
                + "<p style='color: #888;'>If you did not register, ignore this email.</p>"
                + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendNotificationEmail(String toEmail, String subject, String title, String message) {
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PhishGuard - " + subject);

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2 style='color: #2c3e50;'>🛡️ " + title + "</h2>"
                + "<p style='color: #333;'>" + message + "</p>"
                + "<br>"
                + "<a href='" + frontendUrl + "' style='background-color: #3498db; color: white; "
                + "padding: 12px 24px; text-decoration: none; border-radius: 5px;'>"
                + "Go to PhishGuard</a>"
                + "<p style='margin-top: 20px; color: #888;'>PhishGuard Security Team</p>"
                + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mail);

        } catch (Exception e) {
            System.err.println("Failed to send notification email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PhishGuard - Password Reset Request");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2 style='color: #2c3e50;'>Password Reset Request 🔐</h2>"
                + "<p>We received a request to reset your PhishGuard password.</p>"
                + "<p>Click the button below to reset your password:</p>"
                + "<a href='" + resetLink + "' style='background-color: #e74c3c; color: white; "
                + "padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;'>"
                + "Reset Password</a>"
                + "<p style='margin-top: 20px; color: #888;'>⚠️ This link expires in 15 minutes.</p>"
                + "<p style='color: #888;'>If you did not request this, ignore this email.</p>"
                + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send reset email: " + e.getMessage());
        }
    }
}