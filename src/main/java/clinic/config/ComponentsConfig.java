package clinic.config;

import clinic.config.eventhandler.CreateAppointmentEmailHandler;
import clinic.config.eventhandler.SaveAppointmentEmailHandler;
import clinic.services.AppointmentService;
import clinic.services.email.EmailServiceImpl;
import clinic.services.repositories.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
public class ComponentsConfig {

    @Bean
    public AppointmentService getAppointmentService(AppointmentRepository appointmentRepository,
                                                    DoctorRepository doctorRepository,
                                                    PatientRepository patientRepository,
                                                    ScheduleRepository scheduleRepository,
                                                    ScheduleDetailRepository scheduleDetailRepository) {
        return new AppointmentService(appointmentRepository,
                doctorRepository, patientRepository, scheduleRepository, scheduleDetailRepository);
    }

    @Bean
    public JavaMailSender getJavaMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("karizmarestapi@gmail.com");
        mailSender.setPassword("wbhwvvpklmtifigg");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public EmailServiceImpl getEmailServiceImpl(JavaMailSender emailSender, SimpleMailMessage template){
        return new EmailServiceImpl(emailSender, template);
    }

    @Bean
    @ConditionalOnBean(value = EmailServiceImpl.class)
    public CreateAppointmentEmailHandler getCreateAppointmentEventHandler(EmailServiceImpl emailService){
        return new CreateAppointmentEmailHandler(emailService);
    }

    @Bean
    @ConditionalOnBean(value = EmailServiceImpl.class)
    public SaveAppointmentEmailHandler getSaveAppointmentEventHandler(EmailServiceImpl emailService){
        return new SaveAppointmentEmailHandler(emailService);
    }

    @Bean
    public SimpleMailMessage templateSimpleMessage() {
        return new SimpleMailMessage();
    }
}
