package clinic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "DOCTOR")
@Setter
@Getter
public class Doctor {
    @Id
    @Column(name = "DOCTOR_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long doctorId;

    @Column(name = "DOCTOR_NAME")
    private String doctorName;

    @ManyToOne
    @JoinColumn(name = "DOCTOR_TYPE_ID", referencedColumnName = "DOCTOR_TYPE_ID")
    private DoctorType doctorType;

    @JsonIgnore
    @OneToMany(mappedBy = "doctor")
    private List<Schedule> doctorSchedules;

    @JsonIgnore
    @OneToMany(mappedBy = "doctor")
    private List<Appointment> doctorAppointments;
}
