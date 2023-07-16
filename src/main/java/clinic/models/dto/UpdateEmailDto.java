package clinic.models.dto;

import clinic.models.Appointment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateEmailDto {
    private Appointment oldAppointment;
    private Appointment newAppointment;
}
