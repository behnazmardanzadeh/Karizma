package clinic.services.email;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.persistence.criteria.CriteriaBuilder;

@AllArgsConstructor
public class EmailServiceImpl implements IEmailService {
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    public SimpleMailMessage template;

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("karizmarestapi@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
    public void preparePatientEmail(String to, String subject, String patientName, String doctorName, Integer duration, String dateTime) {
        StringBuilder builder = new StringBuilder();
        builder.append("مراجع عزیز،").append("\n");
        builder.append("%s").append("\n");
        builder.append("قرار ملاقات شما با دکتر").append("\n");
        builder.append("%s").append("\n");
        builder.append("در تاریخ و ساعت").append("\n");
        builder.append("%s").append("\n");
        builder.append("و به مدت زمان").append("\n");
        builder.append("%d").append("\n");
        builder.append("دقیقه با موفقیت تنظیم شد.").append("\n");
        builder.append("لطفا راس ساعت تعیین شده، حضور به هم رسانید.").append("\n\n");
        builder.append("«درمانگاه کاریزما»");
        template.setText(builder.toString());
        sendEmail(to, subject, patientName, doctorName, dateTime, duration);
    }

    public void prepareDoctorEmail(String to, String subject, String doctorName, String patientName, Integer duration, String dateTime) {
        StringBuilder builder = new StringBuilder();
        builder.append("پزشک گرامی، ");
        builder.append("آقای/خانم").append("\n");
        builder.append("%s").append("\n");
        builder.append("قرار ملاقات با شما برای بیمار").append("\n");
        builder.append("%s").append("\n");
        builder.append(" در تاریخ و ساعت").append("\n");
        builder.append("%s").append("\n");
        builder.append("و به مدت زمان").append("\n");
        builder.append("%d").append("\n");
        builder.append("دقیقه با موفقیت تنظیم شد.").append("\n\n");
        builder.append("«درمانگاه کاریزما»");
        template.setText(builder.toString());
        sendEmail(to, subject, doctorName, patientName, dateTime, duration);
    }

    public void prepareUpdatePatientEmail(String to, String subject, String patientName, String doctorName, String dateTime, Integer duration, String changes){
        StringBuilder builder = new StringBuilder();
        builder.append("مراجع عزیز،").append("\n");
        builder.append("%s").append("\n");
        builder.append("قرار ملاقات شما با دکتر").append("\n");
        builder.append("%s").append("\n");
        builder.append("در تاریخ و ساعت").append("\n");
        builder.append("%s").append("\n");
        builder.append("و به مدت زمان").append("\n");
        builder.append("%d").append("\n");
        builder.append("دقیقه ");
        builder.append("%s");
        builder.append("تغییر کرد.").append("\n");
        builder.append("لطفا راس ساعت تعیین شده، حضور به هم رسانید.").append("\n\n");
        builder.append("«درمانگاه کاریزما»");
        template.setText(builder.toString());
        String text = String.format(template.getText(), patientName, doctorName, dateTime, duration, changes);
        sendSimpleMessage(to, subject, text);
    }
    public void prepareUpdateDoctorEmail(String to, String subject, String patientName, String doctorName, String dateTime, Integer duration, String changes){
        StringBuilder builder = new StringBuilder();
        builder.append("پزشک گرامی، ");
        builder.append("آقای/خانم").append("\n");
        builder.append("%s").append("\n");
        builder.append("قرار ملاقات با شما برای بیمار").append("\n");
        builder.append("%s").append("\n");
        builder.append(" در تاریخ و ساعت").append("\n");
        builder.append("%s").append("\n");
        builder.append("و به مدت زمان").append("\n");
        builder.append("%d").append("\n");
        builder.append("دقیقه ");
        builder.append("%s");
        builder.append("تغییر کرد.").append("\n\n");
        builder.append("«درمانگاه کاریزما»");
        template.setText(builder.toString());
        String text = String.format(template.getText(), doctorName, patientName, dateTime, duration, changes);
        sendSimpleMessage(to, subject, text);
    }
    public void sendEmail(String to, String subject, String patientName, String doctorName, String dateTime, Integer duration) {
        String text = String.format(template.getText(), patientName, doctorName, dateTime, duration);
        sendSimpleMessage(to, subject, text);
    }
}
