package com.sp.app.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

// spring.mail.host 가 설정된 경우에만 등록되도록 설정

@Component
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class SignupVerificationMailSender {

	private final JavaMailSender javaMailSender;
	private final String senderEmail;
	
	public SignupVerificationMailSender(
			JavaMailSender javaMailSender, 
			@Value("${spring.mail.username}") String senderEmail) {
		this.javaMailSender = javaMailSender;
		this.senderEmail = senderEmail;
	}
	
	// 인증번호 받기
	public void sendVerificationMail(String receiverEmail, String code) {
		sendMail(
				receiverEmail,
				"[Wantmedi] 이메일 인증번호",
				"인증번호는 " + code + "입니다.\n5분 안에 입력해 주세요.");
	}
	// 첫 비밀번호
	public void sendPasswordSetupVerificationMail(String receiverEmail, String code) {
		sendMail(
				receiverEmail,
				"[Wantmedi] 첫 비밀번호 설정 인증번호",
				"인증번호는 " + code + "입니다.\n첫 비밀번호 설정 화면에 입력해 주세요.");
	}
	
	private void sendMail(String receiverEmail, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(senderEmail);
		message.setTo(receiverEmail);
		message.setSubject(subject);
		message.setText(body);

		javaMailSender.send(message);
	}
	
}
