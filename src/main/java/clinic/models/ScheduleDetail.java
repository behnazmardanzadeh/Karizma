package clinic.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE_DETAIL")
@Getter
@Setter
public class ScheduleDetail {
    @Id
    @Column(name = "SCHEDULE_DETAIL_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long scheduleDetailId;

    @Column(name = "SCHEDULE_DATE_TIME_START")
    private LocalDateTime scheduleDateTimeStart;

    @Column(name = "SCHEDULE_DATE_TIME_END")
    private LocalDateTime scheduleDateTimeEnd;

    @ManyToOne
    @JoinColumn(name = "SCHEDULE_ID", referencedColumnName = "SCHEDULE_ID")
    private Schedule schedule;
}
