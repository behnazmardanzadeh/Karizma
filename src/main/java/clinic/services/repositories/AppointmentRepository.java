package clinic.services.repositories;

import clinic.models.Appointment;
import clinic.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAppointmentsByDoctor(Doctor doctor);
}
