package com.hollywood.sweetspot.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mail;

    @Value("${spring.mail.username}")
    private String from;

    public MailService(JavaMailSender mail) {
        this.mail = mail;
    }

    public void sendPasswordReset(String to, String link) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setFrom(from);
        msg.setSubject("[SweetSpot] 비밀번호 재설정 링크");
        msg.setText("""
                안녕하세요. SweetSpot 입니다.

                아래 링크를 열어 비밀번호를 재설정해 주세요.
                (30분간 유효합니다)

                %s

                본 메일을 요청하지 않았다면 무시하셔도 됩니다.
                """.formatted(link));
        mail.send(msg);
    }
}