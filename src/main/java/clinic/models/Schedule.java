package clinic.models;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule implements IEntity{
    @Id
    @Column(name = "SCHEDULE_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long scheduleId;

    @Column(name = "SCHEDULE_START_DATE")
    private LocalDateTime scheduleStartDate;

    @Column(name = "SCHEDULE_END_DATE")
    private LocalDateTime scheduleEndDate;

    @ManyToOne
    @JoinColumn(name = "DOCTOR_ID", referencedColumnName = "DOCTOR_ID")
    private Doctor doctor;
}
