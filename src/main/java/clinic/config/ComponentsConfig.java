package clinic.config;

import clinic.services.AppointmentService;
import clinic.services.repositories.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComponentsConfig {

    @Bean
    public AppointmentService getAppointmentService(AppointmentRepository appointmentRepository,
                                                    DoctorRepository doctorRepository,
                                                    PatientRepository patientRepository,
                                                    ScheduleRepository scheduleRepository) {
        return new AppointmentService(appointmentRepository,
                doctorRepository, patientRepository, scheduleRepository);
    }
}
