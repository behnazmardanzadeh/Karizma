package clinic.services.email;

public interface IEmailService {
    void sendSimpleMessage(String to, String subject, String text);

    void sendEmail(String to, String subject, String patientName, String doctorName, String dateTime, Integer duration);
}
