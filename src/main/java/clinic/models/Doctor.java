package clinic.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "DOCTOR")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Doctor implements IEntity{
    @Id
    @Column(name = "DOCTOR_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long doctorId;

    @Column(name = "DOCTOR_NAME")
    private String doctorName;

    @Column(name = "DOCTOR_EMAIL")
    private String doctorEmail;

    @ManyToOne
    @JoinColumn(name = "DOCTOR_TYPE_ID", referencedColumnName = "DOCTOR_TYPE_ID")
    private DoctorType doctorType;
}
