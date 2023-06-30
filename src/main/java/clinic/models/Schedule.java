package clinic.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "SCHEDULE")
@Getter
@Setter
public class Schedule {
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

    @OneToMany(mappedBy = "schedule")
    private List<ScheduleDetail> scheduleDetails;
}
