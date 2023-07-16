package clinic.config.eventhandler;

import clinic.models.Appointment;
import clinic.models.Patient;
import clinic.models.dto.UpdateEmailDto;
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
public class SaveAppointmentEmailHandler extends AbstractRepositoryEventListener<UpdateEmailDto> {
    @Autowired
    private EmailServiceImpl emailService;

    @Override
    protected void onAfterSave(UpdateEmailDto emailDto) {
        Appointment oldAppointment = emailDto.getOldAppointment();
        Appointment newAppointment = emailDto.getNewAppointment();
        Patient patient = oldAppointment.getPatient();
        String patientEmail = patient.getPatientEmail();
        String patientName = patient.getPatientName() != null ? patient.getPatientName().trim() : "";
        String patientChanges = " ";
        String doctorChanges = " ";
        Long oldDoctorId = oldAppointment.getDoctor().getDoctorId();
        String oldDoctorName = oldAppointment.getDoctor().getDoctorName();
        Long newDoctorId = newAppointment.getDoctor().getDoctorId();
        String doctorName = newAppointment.getDoctor().getDoctorName() != null ? " " + newAppointment.getDoctor().getDoctorName().trim() : " ";
        if (oldDoctorId != newDoctorId) {
            patientChanges += "به دکتر " + "\n" + doctorName + "\n";
            doctorChanges += "به دکتر " + "\n" + doctorName+ "\n" + "ارجاع داده شد." + "\n";
        }
        String oldStartDateTime = oldAppointment.getStartDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String newStartDateTime = newAppointment.getStartDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (!oldStartDateTime.equals(newStartDateTime)) {
            newStartDateTime = newStartDateTime.replace("T", " ");
            patientChanges = addAnd(patientChanges);
            patientChanges += "به تاریخ و ساعت زیر" + "\n" + newStartDateTime + "\n";
            doctorChanges = addAnd(doctorChanges);
            doctorChanges += "به تاریخ و ساعت زیر" + "\n" + newStartDateTime + "\n";
        }
        Integer oldDuration = oldAppointment.getDurationMinutes();
        Integer newDuration = newAppointment.getDurationMinutes();
        if (oldDuration != newDuration) {
            patientChanges = addAnd(patientChanges);
            patientChanges += "مدت زمان ملاقات به" + "\n";
            patientChanges += newDuration + "\n";
            patientChanges += "دقیقه ";

            doctorChanges = addAnd(doctorChanges);
            doctorChanges += "مدت زمان ملاقات به" + "\n";
            doctorChanges += newDuration + "\n";
            doctorChanges += "دقیقه ";
        }
        oldStartDateTime = oldStartDateTime.replace("T", " ");
        if (patientEmail != null && !patientEmail.trim().isEmpty()) {
            emailService.prepareUpdatePatientEmail(patientEmail,
                    "اطلاع رسانی درمانگاه کاریزما",
                    patientName,
                    oldDoctorName,
                    oldStartDateTime,
                    oldDuration,
                    patientChanges);
        }
        String doctorEmail = newAppointment.getDoctor().getDoctorEmail();
        if (doctorEmail != null && !doctorEmail.trim().isEmpty()) {
            emailService.prepareUpdateDoctorEmail(doctorEmail,
                    "اطلاع رسانی درمانگاه کاریزما",
                    patientName,
                    oldDoctorName,
                    oldStartDateTime,
                    oldDuration,
                    doctorChanges);
        }
    }

    private static String addAnd(String changes) {
        if (!changes.trim().isEmpty()) {
            changes += "و ";
        }
        return changes;
    }
}
