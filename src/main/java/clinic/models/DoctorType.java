package clinic.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "DOCTOR_TYPE")
@Data
public class DoctorType implements IEntity{
    @Id
    @Column(name = "DOCTOR_TYPE_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long doctorTypeId;

    @Column(name = "DOCTOR_TYPE_CODE")
    private String doctorTypeCode;

    @Column(name = "DOCTOR_TYPE_NAME")
    private String doctorTypeName;

    @Column(name = "DOCTOR_TYPE_DURATION_MIN")
    private Integer doctorTypeDurationMin;

    @Column(name = "DOCTOR_TYPE_DURATION_MAX")
    private Integer doctorTypeDurationMax;

    @Column(name = "DOCTOR_TYPE_OVERLAPPING_APPOINTMENTS")
    private Integer doctorTypeOverlappingAppointments;
}
