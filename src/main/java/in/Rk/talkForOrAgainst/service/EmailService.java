package in.Rk.talkForOrAgainst.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name){
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Our Platform");
        message.setText("Hello "+name+",\n\nThanks for registering with us!\n\nRegards,\n RK team");
        mailSender.send(message);
    }

    public void sendResetOtpEmail(String toEmail, String otp){
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password reset opt");
        message.setText("Your Otp for resetting the password is"+otp);
        mailSender.send(message);
    }

    public void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Account verification Opt");
        message.setText("You OTP is "+otp+". Verify your account using this OTP.");
        mailSender.send(message);
    }

    public void sendAddedToDebateEmail(String toEmail, String userName, String topic, String debateId) {
        String subject = "You have been added to a debate!";
        String body = String.format(
                "Hello %s,\n\nYou have been added to a debate on the topic: \"%s\".\nPlease log in to participate.\n\n And join with:\"%s\".\n\nThank you!",
                userName, topic, debateId
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

}
