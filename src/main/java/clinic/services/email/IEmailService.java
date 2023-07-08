package clinic.services.email;

public interface IEmailService {
    void sendSimpleMessage(String to, String subject, String text);
}
