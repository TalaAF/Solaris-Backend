package com.example.lms.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

@Configuration
@EnableScheduling
@EnableAsync
public class NotificationConfig {
    
    /**
     * Configure JavaMailSender bean if not already configured elsewhere
     * This can be customized based on your specific mail server requirements
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.example.com"); // Replace with your SMTP server
        mailSender.setPort(587);
        mailSender.setUsername("your-username"); // Replace with your username
        mailSender.setPassword("your-password"); // Replace with your password
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}