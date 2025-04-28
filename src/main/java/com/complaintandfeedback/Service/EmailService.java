package com.complaintandfeedback.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com"); // same as spring.mail.username
        message.setTo(toEmail);
        message.setSubject("Grabobite OTP Verification Code");

        String mailBody = String.format(
                "Hello 👋, \n\n" +
                "Your OTP verification code is: %s\n\n" +
                "This OTP is valid for only 5 minutes. Please do not share it with anyone.\n\n" +
                "If you did not request this, please ignore this message.\n\n" +
                "", otp);

        message.setText(mailBody);
        mailSender.send(message);
    }

}

