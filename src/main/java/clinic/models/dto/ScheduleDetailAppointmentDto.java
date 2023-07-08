package clinic.models.dto;

import clinic.models.*;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.math.BigInteger;

public class ScheduleDetailAppointmentDto {
    private ScheduleDetail scheduleDetail;
    private Appointment appointment;

    public ScheduleDetailAppointmentDto(BigInteger scheduleDetailId,
                                        Date scheduleDateTimeStart,
                                        Date scheduleDateTimeEnd,
                                        BigInteger scheduleId,
                                        BigInteger appointmentId,
                                        Integer durationMinutes,
                                        Date startDateTime,
                                        BigInteger doctorId,
                                        BigInteger patientId){
        Schedule schedule = Schedule.builder().scheduleId(scheduleId.longValue()).build();
        this.scheduleDetail =
                ScheduleDetail.builder()
                        .scheduleDetailId(scheduleDetailId.longValue())
                        .scheduleDateTimeStart(LocalDateTime.ofInstant(scheduleDateTimeStart.toInstant(), ZoneId.systemDefault()))
                        .scheduleDateTimeEnd(LocalDateTime.ofInstant(scheduleDateTimeEnd.toInstant(), ZoneId.systemDefault()))
                        .schedule(schedule)
                        .build();
        if (appointmentId != null) {
            Doctor doctor = Doctor.builder().doctorId(doctorId.longValue()).build();
            Patient patient = Patient.builder().patientId(patientId.longValue()).build();
            this.appointment =
                    Appointment.builder()
                            .appointmentId(appointmentId.longValue())
                            .startDateTime(LocalDateTime.ofInstant(startDateTime.toInstant(), ZoneId.systemDefault()))
                            .durationMinutes(durationMinutes)
                            .scheduleDetail(scheduleDetail)
                            .doctor(doctor)
                            .patient(patient)
                            .build();
        }
    }

    public ScheduleDetail getScheduleDetail() {
        return scheduleDetail;
    }

    public Appointment getAppointment() {
        return appointment;
    }
}
