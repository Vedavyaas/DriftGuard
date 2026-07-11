package com.pheonix.authentication.service;

import com.pheonix.authentication.repository.UserEntity;
import com.pheonix.authentication.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SimpleMailSender {
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public SimpleMailSender(UserRepository userRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @KafkaListener(topics = "mail_listener", groupId = "mailGroup")
    public void listen(String message) {
        String[] messageInfo = message.split(" "); //manager name + " " + message to be delivered
        Optional<UserEntity> userEntity = userRepository.findByUsername(messageInfo[0]);
        if (userEntity.isEmpty()) {
            //assume background error
            return;
        }

        String emailId = userEntity.get().getEmail();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setFrom("noreply@driftguard.com");
        simpleMailMessage.setTo(emailId);
        simpleMailMessage.setSubject("DriftGuard Alert: Critical Infrastructure Drift Detected!");
        simpleMailMessage.setText("URGENT: " + messageInfo[1]);

        mailSender.send(simpleMailMessage);
    }
}