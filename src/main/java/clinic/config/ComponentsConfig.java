package clinic.config;

import clinic.config.eventhandler.AbstractRepositoryEventListener;
import clinic.config.eventhandler.AfterSaveEvent;
import clinic.config.eventhandler.SaveAppointmentEventHandler;
import clinic.models.Appointment;
import clinic.services.AppointmentService;
import clinic.services.email.SaveAppointmentEmailServiceImpl;
import clinic.services.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Configuration
public class ComponentsConfig {
    private SaveAppointmentEventHandler saveAppointmentEventHandler;
    private SaveAppointmentEmailServiceImpl saveAppointmentEmailService;

//    @PostConstruct
//    @ConditionalOnBean
//    public void init() {
//        saveAppointmentEventHandler = new SaveAppointmentEventHandler(saveAppointmentEmailService);
//    }

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
        mailSender.setPassword("A123456a!");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Bean
    public SaveAppointmentEmailServiceImpl getSaveAppointmentEmailServiceImpl(JavaMailSender emailSender){
        SaveAppointmentEmailServiceImpl saveAppointmentEmailService = new SaveAppointmentEmailServiceImpl(emailSender);
        this.saveAppointmentEmailService = saveAppointmentEmailService;
        return saveAppointmentEmailService;
    }

    @Bean
    @ConditionalOnBean(value = SaveAppointmentEmailServiceImpl.class)
    public SaveAppointmentEventHandler getSaveAppointmentEventHandler(SaveAppointmentEmailServiceImpl saveAppointmentEmailService){
        this.saveAppointmentEventHandler = new SaveAppointmentEventHandler(saveAppointmentEmailService);
        return saveAppointmentEventHandler;
    }

    @ConditionalOnBean(value = SaveAppointmentEventHandler.class)
    @EventListener
    public void setSaveAppointmentEventHandler(AfterSaveEvent afterSaveEvent) {
        saveAppointmentEventHandler.onApplicationEvent(afterSaveEvent);
    }
}
