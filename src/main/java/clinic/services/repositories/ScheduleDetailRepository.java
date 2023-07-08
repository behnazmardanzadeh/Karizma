package clinic.services.repositories;

import clinic.models.Appointment;
import clinic.models.Schedule;
import clinic.models.ScheduleDetail;
import clinic.models.dto.ScheduleDetailAppointmentDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleDetailRepository extends JpaRepository<ScheduleDetail, Long> {
    List<ScheduleDetail> findScheduleDetailByScheduleOrderByScheduleDateTimeStartAsc(Schedule schedule);
    List<ScheduleDetail>
    findScheduleDetailByScheduleAndScheduleDateTimeStartLessThanEqualAndAndScheduleDateTimeEndGreaterThanEqual(Schedule schedule,
                                                                                                            LocalDateTime startDateTime,
                                                                                                               LocalDateTime appointmentDateTime);
    @Query(nativeQuery = true)
    Optional<List<ScheduleDetailAppointmentDto>> findAppointmentByScheduleId(@Param("scheduleId") Long scheduleId);
}
