package clinic;

import clinic.common.Consts;
import clinic.common.Routes;
import clinic.models.Appointment;
import clinic.models.Doctor;
import clinic.models.Patient;
import clinic.models.Schedule;
import clinic.services.AppointmentService;
import clinic.services.repositories.DoctorRepository;
import clinic.services.repositories.PatientRepository;
import clinic.services.repositories.ScheduleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AppointmentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenAppointmentObject_whenSetAppointment_thenReturnSavedAppointment() throws Exception{
        //given - precondition or setup
        Long doctorId = 103L;
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() ->
                new IllegalArgumentException("Doctor with doctorId = " + doctorId + " not found."));
        Long patientId = 107L;
        Patient patient = patientRepository.findById(patientId).orElseThrow(() ->
                new IllegalArgumentException("Patient with patientId = " + patientId + " not found."));
        String startDateTimeSt = "2023-07-01T09:00";
        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .startDateTime(startDateTime)
                .durationMinutes(15)
                .build();

        //when - calling api api/setAppointment
        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointment))
        );

        //then - verify the result or output using
    }
}
