package com.yqh.forum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    //void sendPasswordResetEmail(String to, String subject, String text);
    void sendTemporaryPasswordEmail(String to, String userName, String newTemporaryPassword);
}