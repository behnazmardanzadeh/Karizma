package clinic;

import clinic.common.Routes;
import clinic.models.Appointment;
import clinic.models.Doctor;
import clinic.models.Patient;
import clinic.models.dto.SetAppointmentDto;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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
        Long patientId = 107L;
        String startDateTimeSt = "2023-07-08T09:00";
        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_DATE_TIME);

        SetAppointmentDto dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .durationMinutes(15)
                .startDateTime(startDateTime)
                .build();

        //when - calling api api/setAppointment
        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(1))
                ;
    }

    @Test
    public void givenOverlappingAppointmentObject_whenSetEarliestAppointment_thenReturnSavedAppointment() throws Exception{
        //given - precondition or setup
        Long doctorId = 104L;
        Long patientId = 107L;
        String startDateTimeSt = "2023-07-09T10:00:00";
        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_DATE_TIME);

        SetAppointmentDto dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .durationMinutes(10)
                .startDateTime(startDateTime)
                .build();

        //when - calling api api/setAppointment
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(1))
                ;

        startDateTimeSt = "2023-07-09T10:11:00";
        startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_DATE_TIME);
        dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(108L)
                .durationMinutes(10)
                .startDateTime(startDateTime)
                .build();

        //when - calling api api/setAppointment
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(2))
                ;

        startDateTimeSt = "2023-07-09T10:22:00";
        startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_DATE_TIME);
        dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(109L)
                .durationMinutes(10)
                .startDateTime(startDateTime)
                .build();

        //when - calling api api/setAppointment
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(3))
        ;

        startDateTimeSt = "2023-07-09T10:33:00";
        startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_DATE_TIME);
        dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(110L)
                .durationMinutes(20)
                .startDateTime(startDateTime)
                .build();

        //when - calling api api/setAppointment
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(4))
        ;

        startDateTimeSt = "2023-07-09T10:54:00";
        startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_DATE_TIME);
        dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(111L)
                .durationMinutes(30)
                .startDateTime(startDateTime)
                .build();

        //when - calling api api/setAppointment
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(5))
        ;

        startDateTimeSt = "2023-07-09T11:25:00";
        startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_DATE_TIME);
        dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(112L)
                .durationMinutes(10)
                .startDateTime(startDateTime)
                .build();

        //when - calling api api/setAppointment
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(6))
        ;

        startDateTimeSt = "2023-07-09T11:36:00";
        startDateTime = LocalDateTime.parse(startDateTimeSt, DateTimeFormatter.ISO_DATE_TIME);
        dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(113L)
                .durationMinutes(24)
                .startDateTime(startDateTime)
                .build();

        //when - calling api api/setAppointment
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Routes.POST_api_setAppointment)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(7))
        ;

        dto = SetAppointmentDto.builder()
                .doctorId(doctorId)
                .patientId(114L)
                .durationMinutes(30)
                .build();

        //when - calling api api/setAppointment
        mockMvc.perform(
                        MockMvcRequestBuilders.post(Routes.POST_api_setEarliestAppointment)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.appointmentId").value(8))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startDateTime").value("2023-07-09T10:22:00"))
        ;
    }
}
