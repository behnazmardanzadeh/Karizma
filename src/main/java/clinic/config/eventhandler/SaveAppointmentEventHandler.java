package clinic.config.eventhandler;

import clinic.models.Appointment;
import clinic.models.Patient;
import clinic.services.email.SaveAppointmentEmailServiceImpl;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.rest.core.event.RepositoryEvent;

import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class SaveAppointmentEventHandler extends AbstractRepositoryEventListener<Appointment>{
    private final Class<?> INTERESTED_TYPE = GenericTypeResolver.resolveTypeArgument(getClass(), AbstractRepositoryEventListener.class);
    @Autowired
    private SaveAppointmentEmailServiceImpl saveAppointmentEmailService;

    @Override
    public void onApplicationEvent(RepositoryEvent event) {
        Class<?> srcType = event.getSource().getClass();
        if (null != INTERESTED_TYPE && !INTERESTED_TYPE.isAssignableFrom(srcType)) {
            return;
        }
        if (event instanceof AfterSaveEvent) {
            onAfterSave((Appointment) event.getSource());
        }
    }

    @Override
    protected void onAfterSave(Appointment entity) {
        log.info("onAfterSave : " + entity.toString());
        Appointment appointment = (Appointment) entity;
        Patient patient = appointment.getPatient();
        String to = "";
        String text = "";
        String patientEmail = patient.getPatientEmail();
        String patientName = patient.getPatientName() != null ? patient.getPatientName().trim() : "";
        String doctorName = appointment.getDoctor().getDoctorName() != null ? " " + appointment.getDoctor().getDoctorName().trim() : " ";
        if (patientEmail != null && !patientEmail.trim().isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("مراجع عزیز، ");
            builder.append(patientName).append("\n");
            builder.append("قرار ملاقات شما با دکتر");
            builder.append(doctorName);
            builder.append(" در تاریخ و ساعت ");
            builder.append(appointment.getStartDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            builder.append(" با موفقیت تنظیم شد.").append("\n");
            builder.append("لطفا راس ساعت تعیین شده، حضور به هم رسانید.").append("\n");
            builder.append("درمانگاه کاریزما");
            saveAppointmentEmailService.sendSimpleMessage(patientEmail,"اطلاع رسانی درمانگاه کاریزما", builder.toString());
        }
        String doctorEmail = appointment.getDoctor().getDoctorEmail();
        if (doctorEmail != null && !doctorEmail.trim().isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("پزشک گرامی، ");
            builder.append("آقای/خانم ");
            builder.append(doctorName).append("\n");
            builder.append("قرار ملاقات با شما برای بیمار ");
            builder.append(patientName);
            builder.append(" در تاریخ و ساعت ");
            builder.append(appointment.getStartDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            builder.append(" با موفقیت تنظیم شد.").append("\n");
            builder.append("درمانگاه کاریزما");
            saveAppointmentEmailService.sendSimpleMessage(doctorEmail, "اطلاع رسانی درمانگاه کاریزما", builder.toString());
        }
    }
}
