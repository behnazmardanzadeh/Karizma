package clinic.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "PATIENT")
@Getter
@Setter
public class Patient {
    @Id
    @Column(name = "PATIENT_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long patientId;

    @Column(name = "PATIENT_NAME")
    private String patientName;

    @JsonIgnore
    @OneToMany(mappedBy = "patient")
    private List<Appointment> patientAppointments;
}
