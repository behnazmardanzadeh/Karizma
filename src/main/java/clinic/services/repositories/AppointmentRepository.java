package clinic.services.repositories;

import clinic.models.Appointment;
import clinic.models.ScheduleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAppointmentsByScheduleDetail(ScheduleDetail scheduleDetail);
    @Query(value = "SELECT \n" +
            "* \n" +
            "FROM APPOINTMENT\n" +
            "where patient_id = :patientId\n" +
            "and to_char(START_DATE_TIME , 'yyyy-mm-dd') = :date", nativeQuery = true)
    List<Appointment> findAppointmentsByPatientAndStartDateTimeEquals(@Param("patientId") Long patientId, @Param("date") String date);
}
