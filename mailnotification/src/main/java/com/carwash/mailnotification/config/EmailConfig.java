package com.carwash.mailnotification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
    
    @Value("${spring.mail.host}")
    public String smtpHost;
    
    @Value("${spring.mail.port}")
    public String smtpPort;
    
    @Value("${spring.mail.username}")
    public String senderEmail;
    
    @Value("${spring.mail.password}")
    public String appPassword;
    
    @Value("${spring.mail.from}")
    public String fromName;
}