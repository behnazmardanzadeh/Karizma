package clinic.services.email;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@AllArgsConstructor
public class SaveAppointmentEmailServiceImpl implements IEmailService {
    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("karizmarestapi@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
//        emailSender.send(message);
    }
}
