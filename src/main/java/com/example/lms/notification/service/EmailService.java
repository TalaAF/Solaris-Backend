package com.example.lms.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@example.com}")
    private String fromEmail;
    
    @Value("${spring.mail.enabled:false}")
    private boolean emailEnabled;
    
    /**
     * Send a simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Would send to: {}, subject: {}", to, subject);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent to: {}, subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send simple email to: " + to, e);
        }
    }
    
    /**
     * Send an HTML email
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Would send HTML email to: {}, subject: {}", to, subject);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML content
            
            mailSender.send(message);
            log.info("HTML email sent to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: " + to, e);
        } catch (Exception e) {
            log.error("Unexpected error when sending HTML email to: " + to, e);
        }
    }
    
    /**
     * Send an HTML email with attachments
     */
    @Async
    public void sendEmailWithAttachment(String to, String subject, String htmlContent, 
                                     String attachmentFileName, byte[] attachmentContent) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Would send email with attachment to: {}, subject: {}", to, subject);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            // Add attachment
            jakarta.mail.util.ByteArrayDataSource attachment = 
                    new jakarta.mail.util.ByteArrayDataSource(attachmentContent, "application/octet-stream");
            helper.addAttachment(attachmentFileName, attachment);
            
            mailSender.send(message);
            log.info("Email with attachment sent to: {}, subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email with attachment to: " + to, e);
        }
    }
}