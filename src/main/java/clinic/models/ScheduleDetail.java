package clinic.models;

import clinic.models.dto.ScheduleDetailAppointmentDto;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE_DETAIL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedNativeQuery(name = "ScheduleDetail.findAppointmentByScheduleId",
        query = "SELECT \n" +
                "sd.SCHEDULE_DETAIL_ID,\n" +
                "sd.SCHEDULE_DATE_TIME_START,\n" +
                "sd.SCHEDULE_DATE_TIME_END, \n" +
                "sd.SCHEDULE_ID,\n" +
                "a.APPOINTMENT_ID,\n" +
                "a.DURATION_MINUTES,\n" +
                "a.START_DATE_TIME,\n" +
                "a.DOCTOR_ID,\n" +
                "a.PATIENT_ID \n" +
                "FROM SCHEDULE_DETAIL sd\n" +
                "left join APPOINTMENT a\n" +
                "on sd.SCHEDULE_DETAIL_ID = a.SCHEDULE_DETAIL_ID \n" +
                "where sd.SCHEDULE_ID = :scheduleId",
        resultSetMapping = "ScheduleDetailAppointmentDto")
@SqlResultSetMapping(name = "ScheduleDetailAppointmentDto",
        classes = @ConstructorResult(targetClass = ScheduleDetailAppointmentDto.class,
                columns = {
                @ColumnResult(name = "SCHEDULE_DETAIL_ID"),
                        @ColumnResult(name = "SCHEDULE_DATE_TIME_START"),
                        @ColumnResult(name = "SCHEDULE_DATE_TIME_END"),
                        @ColumnResult(name = "SCHEDULE_ID"),
                        @ColumnResult(name = "APPOINTMENT_ID"),
                        @ColumnResult(name = "DURATION_MINUTES"),
                        @ColumnResult(name = "START_DATE_TIME"),
                        @ColumnResult(name = "DOCTOR_ID"),
                        @ColumnResult(name = "PATIENT_ID")
        }))
public class ScheduleDetail implements IEntity{
    @Id
    @Column(name = "SCHEDULE_DETAIL_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long scheduleDetailId;

    @Column(name = "SCHEDULE_DATE_TIME_START")
    private LocalDateTime scheduleDateTimeStart;

    @Column(name = "SCHEDULE_DATE_TIME_END")
    private LocalDateTime scheduleDateTimeEnd;

    @ManyToOne
    @JoinColumn(name = "SCHEDULE_ID", referencedColumnName = "SCHEDULE_ID")
    private Schedule schedule;
}
