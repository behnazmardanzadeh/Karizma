package clinic.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SetAppointmentDto {
private Long doctorId;
private Long patientId;
private Integer durationMinutes;
private LocalDateTime startDateTime;
}
