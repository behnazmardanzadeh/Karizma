package clinic.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "PATIENT")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient implements IEntity{
    @Id
    @Column(name = "PATIENT_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long patientId;

    @Column(name = "PATIENT_NAME")
    private String patientName;

    @Column(name = "PATIENT_EMAIL")
    private String patientEmail;
}
