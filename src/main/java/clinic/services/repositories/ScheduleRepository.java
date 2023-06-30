package clinic.services.repositories;

import clinic.models.Doctor;
import clinic.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findSchedulesByDoctorAndScheduleEndDateGreaterThanEqualAndScheduleStartDateLessThanEqual(Doctor doctor, LocalDateTime startDateTime, LocalDateTime appointmentDate);
    List<Schedule> findSchedulesByDoctorAndScheduleStartDateGreaterThanEqualOrderByScheduleStartDateAsc(Doctor doctor, LocalDateTime startDateTime);
}
