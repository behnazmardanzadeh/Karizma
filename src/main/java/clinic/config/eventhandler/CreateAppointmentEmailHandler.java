package clinic.config.eventhandler;

import clinic.models.Appointment;
import clinic.models.Patient;
import clinic.services.email.EmailServiceImpl;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;

import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CreateAppointmentEmailHandler extends AbstractRepositoryEventListener<Appointment> {
    @Autowired
    private EmailServiceImpl emailService;

    @Override
    protected void onAfterCreate(Appointment appointment) {
        Patient patient = appointment.getPatient();
        String patientEmail = patient.getPatientEmail();
        String patientName = patient.getPatientName() != null ? patient.getPatientName().trim() : "";
        String doctorName = appointment.getDoctor().getDoctorName() != null ? " " + appointment.getDoctor().getDoctorName().trim() : " ";
        String startDateTime = appointment.getStartDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        startDateTime = startDateTime.replace("T", " ");
        Integer duration = appointment.getDurationMinutes();
        if (patientEmail != null && !patientEmail.trim().isEmpty()) {
            emailService.preparePatientEmail(patientEmail,
                    "اطلاع رسانی درمانگاه کاریزما",
                    patientName,
                    doctorName,
                    duration,
                    startDateTime);
        }
        String doctorEmail = appointment.getDoctor().getDoctorEmail();
        if (doctorEmail != null && !doctorEmail.trim().isEmpty()) {
            emailService.prepareDoctorEmail(doctorEmail, "اطلاع رسانی درمانگاه کاریزما", doctorName, patientName, duration, startDateTime);
        }
    }
}
