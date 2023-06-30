package clinic.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "APPOINTMENT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @Column(name = "APPOINTMENT_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long appointmentId;

    @Column(name = "DURATION_MINUTES")
    private Integer durationMinutes;

    @Column(name = "START_DATE_TIME")
    private LocalDateTime startDateTime;

    @ManyToOne
    @JoinColumn(name = "DOCTOR_ID", referencedColumnName = "DOCTOR_ID")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "PATIENT_ID", referencedColumnName = "PATIENT_ID")
    private Patient patient;
}
