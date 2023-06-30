package clinic.services;

import clinic.common.Consts;
import clinic.models.*;
import clinic.models.dto.SetAppointmentDto;
import clinic.services.repositories.AppointmentRepository;
import clinic.services.repositories.DoctorRepository;
import clinic.services.repositories.PatientRepository;
import clinic.services.repositories.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              ScheduleRepository scheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public Appointment saveAppointment(SetAppointmentDto setAppointmentDto) {
        Doctor doctor = getAppointmentDoctor(setAppointmentDto.getDoctorId());
        Patient patient = getAppointmentPatient(setAppointmentDto.getPatientId());

        Integer durationMinutes = setAppointmentDto.getDurationMinutes();
        validateDurationInMinutes(durationMinutes, doctor);
        LocalDateTime startDateTime = setAppointmentDto.getStartDateTime();
        validateStartDateTime(startDateTime);
        checkDoctorPresenceInClinic(startDateTime, doctor);
        List<Appointment> patientAppointments = checkPatientAppointmentsCount(startDateTime.toLocalDate(), patient);
        if (patientAppointments != null && !patientAppointments.isEmpty()) {
            checkPatientAppointmentsOverlap(startDateTime.toLocalTime(), patientAppointments, patient.getPatientId());
        }
        LocalDateTime correctedStartDateTime = checkDoctorAppointmentsOverlap(doctor, startDateTime, durationMinutes);

        Appointment savedAppointment = appointmentRepository.save(
                Appointment.builder()
                        .doctor(doctor)
                        .patient(patient)
                        .durationMinutes(durationMinutes)
                        .startDateTime(correctedStartDateTime)
                        .build());
        return savedAppointment;
    }

    public Appointment saveEarliestAppointment(SetAppointmentDto setAppointmentDto) {
        Doctor doctor = getAppointmentDoctor(setAppointmentDto.getDoctorId());
        Patient patient = getAppointmentPatient(setAppointmentDto.getPatientId());
        Integer durationMinutes = setAppointmentDto.getDurationMinutes();
        validateDurationInMinutes(durationMinutes, doctor);
        LocalDateTime startDateTime = getEarliest(doctor, durationMinutes);
        List<Appointment> patientAppointments = checkPatientAppointmentsCount(startDateTime.toLocalDate(), patient);
        if (patientAppointments != null && !patientAppointments.isEmpty()) {
            checkPatientAppointmentsOverlap(startDateTime.toLocalTime(), patientAppointments, patient.getPatientId());
        }
        Appointment savedAppointment = appointmentRepository.save(
                Appointment.builder()
                        .doctor(doctor)
                        .patient(patient)
                        .durationMinutes(durationMinutes)
                        .startDateTime(startDateTime)
                        .build());
        return savedAppointment;
    }

    private LocalDateTime getEarliest(Doctor doctor, Integer durationMinutes) {
        LocalDateTime today = LocalDateTime.now();
        List<Schedule> doctorSchedules =
                scheduleRepository
                        .findSchedulesByDoctorAndScheduleEndDateGreaterThanEqualAndScheduleStartDateLessThanEqual(
                                doctor,
                                today,
                                today);
        if (doctorSchedules == null || doctorSchedules.isEmpty()) {
            doctorSchedules =
                    scheduleRepository
                            .findSchedulesByDoctorAndScheduleStartDateGreaterThanEqualOrderByScheduleStartDateAsc(
                                    doctor,
                                    today);
        }
        if (doctorSchedules == null || doctorSchedules.isEmpty()) {
            throw new IllegalArgumentException("No Schedule found by doctorId : "
                    + doctor.getDoctorId() + " and date greater than or equal today.");
        }
        Schedule schedule = doctorSchedules.get(0);
        List<ScheduleDetail> details =
                schedule.getScheduleDetails().stream()
                        .sorted(Comparator.comparing(ScheduleDetail::getScheduleDateTimeStart))
                        .collect(Collectors.toList());

        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("No ScheduleDetail found by doctorId : "
                    + doctor.getDoctorId() + " and date greater than or equal today.");
        }

        LocalDateTime appointmentDateTime = details.get(0).getScheduleDateTimeStart();

        List<Appointment> doctorAppointments = doctor.getDoctorAppointments();
        if (doctorAppointments != null && !doctorAppointments.isEmpty()) {
            boolean notFound = true;
            for (int dIndex = 0; dIndex < details.size() && notFound; dIndex++) {
                ScheduleDetail detail = details.get(dIndex);
                appointmentDateTime = detail.getScheduleDateTimeStart();
                List<Appointment> appointments = doctorAppointments.stream().filter(appointment -> {
                            LocalDateTime appointmentStartDateTime = appointment.getStartDateTime();
                            LocalDateTime detailStart = detail.getScheduleDateTimeStart();
                            return appointmentStartDateTime.isAfter(detailStart)
                                    || appointmentStartDateTime.isEqual(detailStart);
                        }).filter(appointment -> {
                            LocalDateTime appointmentStartDateTime = appointment.getStartDateTime();
                            LocalDateTime detailEnd = detail.getScheduleDateTimeEnd();
                            return appointmentStartDateTime.isBefore(detailEnd)
                                    || appointmentStartDateTime.isEqual(detailEnd);
                        }).sorted(Comparator.comparing(Appointment::getStartDateTime))
                        .collect(Collectors.toList());
                if (appointments == null || appointments.isEmpty()) {
                    notFound = false;
                }
                int overlapCounter = 0;
                /**
                 * How come doctor.getDoctorType().getDoctorTypeOverlappingAppointments() - 1 ?
                 * 1 overlap includes 2 appointments
                 */
                Integer doctorTypeOverlappingAppointments = doctor.getDoctorType().getDoctorTypeOverlappingAppointments();
                boolean isOverlapAllowed = doctorTypeOverlappingAppointments > 0;
                int numberOfAllowedOverlap = doctorTypeOverlappingAppointments > 0 ? doctorTypeOverlappingAppointments - 1 : 0;
                LocalDateTime previousAppointmentEnd = null;
                for (int i = 0; i < appointments.size() && notFound; i++) {
                    Appointment appointment = appointments.get(i);
                    LocalDateTime appointmentStart = appointment.getStartDateTime();
                    LocalDateTime appointmentEnd = appointmentStart.plusMinutes(appointment.getDurationMinutes());
                    if (isOverlapAllowed && isOverlappingAppointment(previousAppointmentEnd, appointmentStart)) {
                        overlapCounter++;
                        if (overlapCounter >= numberOfAllowedOverlap) {
                            isOverlapAllowed = false;
                        }
                    }
                    if (previousAppointmentEnd != null) {
                        /**
                         * as mentioned before in "getAppointmentsInScheduleDetailRange" method,
                         * plusMinutes(1) is added to appointmentEnd
                         * in order to avoid overlapping.
                         */
                        LocalDateTime earliestAppointmentStart = previousAppointmentEnd.plusMinutes(1);
                        if (earliestAppointmentStart.plusMinutes(durationMinutes).isBefore(appointmentStart)) {
                            appointmentDateTime = earliestAppointmentStart;
                            notFound = false;
                        }
                    }
                    previousAppointmentEnd = appointmentEnd;
                }
                if (notFound && isOverlapAllowed) {
                    int windowSize = numberOfAllowedOverlap;
                    int earliestAppointmentDuration = durationMinutes;
                    int startIndex = 0;
                    int currentIndex = 0;
                    while(currentIndex < appointments.size()) {
                        for (int i = currentIndex; i < (startIndex + windowSize) && notFound; i++) {
                            currentIndex = i;
                            Appointment appointment = appointments.get(i);
                            Integer appointmentDurationMinutes = appointment.getDurationMinutes();
                            if (appointmentDurationMinutes >= earliestAppointmentDuration) {
                                notFound = false;
                                currentIndex = appointments.size();
                            } else {
                                earliestAppointmentDuration -= appointmentDurationMinutes;
                                if (earliestAppointmentDuration <= 0) {
                                    notFound = false;
                                    currentIndex = appointments.size();
                                }
                            }
                        }
                        if (notFound) {
                            startIndex = currentIndex;
                            earliestAppointmentDuration = durationMinutes;
                        }
                    }
                    appointmentDateTime = appointments.get(startIndex).getStartDateTime();
                }
            }
        }
        return appointmentDateTime;
    }

    private void validateStartDateTime(LocalDateTime startDateTime) {
        if (startDateTime == null) {
            throw new IllegalArgumentException("startDateTime is null.");
        }
        LocalDateTime today = LocalDateTime.now();
        if (startDateTime.isBefore(today)) {
            throw new RuntimeException("startDateTime is invalid. startDateTime is before today date : "
                    + today.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (startDateTime.getDayOfWeek().getValue() == DayOfWeek.THURSDAY.getValue()
                || startDateTime.getDayOfWeek().getValue() == DayOfWeek.FRIDAY.getValue()) {
            throw new RuntimeException("startDateTime is invalid. Valid Days are between Saturday to Wednesday.");
        }
    }

    private void checkDoctorPresenceInClinic(LocalDateTime startDateTime, Doctor doctor) {
        List<Schedule> schedules = checkDoctorSchedule(doctor, startDateTime);
        Schedule schedule = schedules.get(0);
        LocalDate appointmentDate = startDateTime.toLocalDate();
        List<ScheduleDetail> availableSchedulesByDate = schedule.getScheduleDetails().stream()
                .filter(scheduleDetail ->
                        scheduleDetail.getScheduleDateTimeStart().toLocalDate().compareTo(appointmentDate) == 0)
                .sorted(Comparator.comparing(ScheduleDetail::getScheduleDateTimeStart))
                .collect(Collectors.toList());
        if (availableSchedulesByDate == null || availableSchedulesByDate.isEmpty()) {
            throw new RuntimeException(
                    "Doctor is not present on date provided in startDateTime : "
                            + startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        LocalTime appointmentTime = startDateTime.toLocalTime();
        List<ScheduleDetail> availableSchedulesByTime = availableSchedulesByDate.stream().filter(scheduleDetail -> {
            LocalTime startTime = scheduleDetail.getScheduleDateTimeStart().toLocalTime();
            LocalTime endTime = scheduleDetail.getScheduleDateTimeEnd().toLocalTime();
            return (startTime.isBefore(appointmentTime) && endTime.isAfter(appointmentTime)) || startTime.equals(appointmentTime);
        }).collect(Collectors.toList());
        if (availableSchedulesByTime == null || availableSchedulesByTime.isEmpty()) {
            throw new RuntimeException(
                    "Doctor is not present on time provided in startDateTime : "
                            + startDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        }
    }

    private List<Appointment> checkPatientAppointmentsCount(LocalDate appointmentDate, Patient patient) {
        List<Appointment> patientAppointments = patient.getPatientAppointments().stream()
                .filter(appointment -> appointment.getStartDateTime().toLocalDate().isEqual(appointmentDate))
                .collect(Collectors.toList());
        if (patientAppointments != null
                && !patientAppointments.isEmpty()
                && patientAppointments.size() == Consts.MAXIMUM_NUMBER_OF_PATIENT_APPOINTMENTS_IN_A_DAY) {
            throw new RuntimeException("Patient already reserved "
                    + Consts.MAXIMUM_NUMBER_OF_PATIENT_APPOINTMENTS_IN_A_DAY +
                    " appointments in provided startDateTime : "
                    + appointmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return patientAppointments;
    }

    private void checkPatientAppointmentsOverlap(LocalTime appointmentTime, List<Appointment> patientAppointments, Long patientId) {
        List<Appointment> patientOverlapAppointments = patientAppointments.stream()
                .filter(appointment -> {
                    LocalTime patientAppointmentTime = appointment.getStartDateTime().toLocalTime();
                    LocalTime patientAppointmentTimePlusDuration = patientAppointmentTime.plusMinutes(appointment.getDurationMinutes());
                    return patientAppointmentTimePlusDuration.equals(appointmentTime)
                            || patientAppointmentTimePlusDuration.isAfter(appointmentTime);
                }).collect(Collectors.toList());
        if (patientOverlapAppointments != null && !patientOverlapAppointments.isEmpty()) {
            throw new RuntimeException(
                    "Provided patientId : " + patientId + " has already booked appointments" +
                            " which are overlapping with time provided in startDateTime : "
                            + appointmentTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
        }
    }

    private LocalDateTime checkDoctorAppointmentsOverlap(Doctor doctor,
                                                         LocalDateTime startDateTime,
                                                         Integer durationMinutes) {
        LocalDateTime correctedStartDateTime = startDateTime;
        LocalDate appointmentDate = startDateTime.toLocalDate();
        List<Appointment> doctorAppointments = getDoctorAppointmentsOnAppointmentDate(doctor, appointmentDate);
        if (doctorAppointments != null && !doctorAppointments.isEmpty()) {
            ScheduleDetail appointmentScheduleDetail = getScheduleDetail(doctor, startDateTime);
            LocalDateTime scheduleDateTimeStart = appointmentScheduleDetail.getScheduleDateTimeStart();
            LocalDateTime scheduleDateTimeEnd = appointmentScheduleDetail.getScheduleDateTimeEnd();
            long minutes = ChronoUnit.MINUTES.between(scheduleDateTimeStart, scheduleDateTimeEnd);
            AtomicInteger totalAvailableTime = new AtomicInteger((int) minutes);
            AtomicInteger totalAppointmentsDuration = new AtomicInteger(0);
            List<Appointment> appointmentsInScheduleDetailRange =
                    getAppointmentsInScheduleDetailRange(
                            doctorAppointments,
                            scheduleDateTimeStart,
                            scheduleDateTimeEnd,
                            totalAvailableTime,
                            totalAppointmentsDuration);

            boolean isOverlappingWithStartDateTime =
                    appointmentsInScheduleDetailRange.stream().anyMatch(appointment ->
                            isOverlappingWithStartDateTime(startDateTime, appointment.getStartDateTime(), appointment.getDurationMinutes()));

            if (totalAvailableTime.intValue() < durationMinutes || isOverlappingWithStartDateTime) {
                correctedStartDateTime = getStartDateTimeAsOverlappingAppointment(
                        doctor,
                        startDateTime,
                        durationMinutes,
                        totalAvailableTime.intValue(),
                        appointmentsInScheduleDetailRange);
            }
        }
        return correctedStartDateTime;
    }

    private ScheduleDetail getScheduleDetail(Doctor doctor, LocalDateTime startDateTime) {
        List<Schedule> doctorSchedules = checkDoctorSchedule(doctor, startDateTime);
        Schedule doctorSchedule = doctorSchedules.get(0);
        List<ScheduleDetail> scheduleDetails = doctorSchedule.getScheduleDetails();
        if (scheduleDetails == null || scheduleDetails.isEmpty()) {
            throw new RuntimeException("No schedule Detail found for doctorId: " + doctor.getDoctorId()
                    + " in startDateTime: " + startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        List<ScheduleDetail> appointmentScheduleDetailList = getScheduleDetailByStartDateTime(startDateTime, scheduleDetails);
        if (appointmentScheduleDetailList == null || appointmentScheduleDetailList.isEmpty()) {
            throw new RuntimeException("No schedule detail found for startDateTime:"
                    + startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (appointmentScheduleDetailList.size() > 1) {
            throw new RuntimeException("Multiple schedule details found for startDateTime:"
                    + startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return appointmentScheduleDetailList.get(0);
    }

    private static LocalDateTime getStartDateTimeAsOverlappingAppointment(
            Doctor doctor,
            LocalDateTime startDateTime,
            Integer durationMinutes,
            Integer totalAvailableTime,
            List<Appointment> appointmentsInScheduleRange) {
        LocalDateTime correctedStartDateTime = startDateTime;
        int overlapCounter = 0;
        LocalDateTime previousAppointmentEnd = null;
        /**
         * How come doctor.getDoctorType().getDoctorTypeOverlappingAppointments() - 1 ?
         * 1 overlap includes 2 appointments
         */
        Integer doctorTypeOverlappingAppointments = doctor.getDoctorType().getDoctorTypeOverlappingAppointments();
        boolean isOverlapAllowed = doctorTypeOverlappingAppointments > 0;
        int numberOfAllowedOverlap = doctorTypeOverlappingAppointments > 0 ? doctorTypeOverlappingAppointments - 1 : 0;
        for (int i = 0; i < appointmentsInScheduleRange.size() && isOverlapAllowed; i++) {
            Appointment appointment = appointmentsInScheduleRange.get(i);
            LocalDateTime appointmentStartDateTime = appointment.getStartDateTime();
            if (isOverlappingAppointment(previousAppointmentEnd, appointmentStartDateTime)) {
                overlapCounter++;
                if (overlapCounter >= numberOfAllowedOverlap) {
                    isOverlapAllowed = false;
                }
            }
            LocalDateTime appointmentEnd = appointmentStartDateTime.plusMinutes(appointment.getDurationMinutes());
            previousAppointmentEnd = appointmentEnd;
        }
        if (!isOverlapAllowed) {
            throw new RuntimeException("Appointment overlaps with registered appointments in startDateTime: "
                    + startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (totalAvailableTime < durationMinutes) {
            int remainderMinutes = durationMinutes - totalAvailableTime;
            correctedStartDateTime = startDateTime.minusMinutes(remainderMinutes);
        }
        return correctedStartDateTime;
    }

    private static List<ScheduleDetail> getScheduleDetailByStartDateTime(LocalDateTime startDateTime, List<ScheduleDetail> scheduleDetails) {
        return scheduleDetails.stream().filter(scheduleDetail -> {
            LocalDateTime scheduleDateTimeStart = scheduleDetail.getScheduleDateTimeStart();
            return scheduleDateTimeStart.isBefore(startDateTime)
                    || scheduleDateTimeStart.isEqual(startDateTime);
        }).filter(scheduleDetail -> {
            LocalDateTime scheduleDateTimeEnd = scheduleDetail.getScheduleDateTimeEnd();
            return scheduleDateTimeEnd.isAfter(startDateTime)
                    || scheduleDateTimeEnd.isEqual(startDateTime);
        }).collect(Collectors.toList());
    }

    private static List<Appointment> getDoctorAppointmentsOnAppointmentDate(Doctor doctor, LocalDate appointmentDate) {
        return doctor.getDoctorAppointments().stream()
                .filter(doctorAppointment -> doctorAppointment.getStartDateTime().toLocalDate().isEqual(appointmentDate))
                .sorted(Comparator.comparing(Appointment::getStartDateTime))
                .collect(Collectors.toList());
    }

    private static List<Appointment> getAppointmentsInScheduleDetailRange(
            List<Appointment> doctorAppointments,
            LocalDateTime scheduleDateTimeStart,
            LocalDateTime scheduleDateTimeEnd,
            AtomicInteger totalAvailableTime,
            AtomicInteger totalAppointmentsDuration) {
        return doctorAppointments.stream().filter(appointment ->
                        appointment.getStartDateTime().isAfter(scheduleDateTimeStart)
                                || appointment.getStartDateTime().isEqual(scheduleDateTimeStart))
                .filter(appointment ->
                        appointment.getStartDateTime().isBefore(scheduleDateTimeEnd)
                                || appointment.getStartDateTime().isEqual(scheduleDateTimeEnd))
                .peek(appointment -> {
                    Integer appointmentDuration = appointment.getDurationMinutes();
                    totalAppointmentsDuration.getAndAdd(appointmentDuration);
                    /**
                     * appointmentDuration + 1 avoids overlapping appointments
                     * example:
                     * appointmentStart: 09:00, duration: 15 min, appointmentEnd: 09:15
                     * totalAvailableTime in doctor schedule: 30 min
                     * totalAvailableTime = 30 min - 15 min = 15 min
                     * Which means next appointment with duration = 15 min,
                     * will start at 09:15 causing overlap with previous appointmentEnd
                     */
                    int updatedAvailableTime = totalAvailableTime.intValue() - (appointmentDuration + 1);
                    totalAvailableTime.getAndSet(updatedAvailableTime);
                })
                .sorted(Comparator.comparing(Appointment::getStartDateTime))
                .collect(Collectors.toList());
    }

    private static boolean isOverlappingWithStartDateTime(LocalDateTime startDateTime, LocalDateTime appointmentStartDateTime, Integer appointmentDuration) {
        return (startDateTime.isAfter(appointmentStartDateTime)
                || startDateTime.isEqual(appointmentStartDateTime))
                && (startDateTime.isBefore(appointmentStartDateTime.plusMinutes(appointmentDuration))
                || startDateTime.isEqual(appointmentStartDateTime.plusMinutes(appointmentDuration)));
    }

    private static boolean isOverlappingAppointment(LocalDateTime previousAppointmentEnd, LocalDateTime appointmentStartDateTime) {
        return previousAppointmentEnd != null
                && (previousAppointmentEnd.isAfter(appointmentStartDateTime)
                || previousAppointmentEnd.isEqual(appointmentStartDateTime));
    }

    private List<Schedule> checkDoctorSchedule(Doctor doctor, LocalDateTime startDateTime) {
        List<Schedule> doctorSchedules = scheduleRepository.
                findSchedulesByDoctorAndScheduleEndDateGreaterThanEqualAndScheduleStartDateLessThanEqual(doctor, startDateTime, startDateTime);
        if (doctorSchedules == null || doctorSchedules.isEmpty()) {
            throw new RuntimeException("No schedule found by doctorId and startDateTime.");
        }
        if (doctorSchedules.size() > 1) {
            throw new RuntimeException("More than one schedule is found by doctor and startDateTime.");
        }
        return doctorSchedules;
    }

    private void validateDurationInMinutes(Integer durationMinutes, Doctor doctor) {
        if (durationMinutes == null) {
            throw new IllegalArgumentException("durationMinutes is null.");
        }
        Integer durationMin = doctor.getDoctorType().getDoctorTypeDurationMin();
        Integer durationMax = doctor.getDoctorType().getDoctorTypeDurationMax();
        if (durationMinutes < durationMin) {
            throw new RuntimeException("durationMinutes is less than minimum. " +
                    "durationMinutes based on selected doctor must be between "
                    + durationMin + " and " + durationMax + ".");
        }
        if (durationMinutes > durationMax) {
            throw new RuntimeException("durationMinutes is more than maximum. " +
                    "durationMinutes based on selected doctor must be between "
                    + durationMin + " and " + durationMax + ".");
        }
    }

    private Doctor getAppointmentDoctor(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId is null.");
        }
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() ->
                new IllegalArgumentException("Doctor with doctorId = " + doctorId + " not found."));
        return doctor;
    }

    private Patient getAppointmentPatient(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is null.");
        }
        Patient patient = patientRepository.findById(patientId).orElseThrow(() ->
                new IllegalArgumentException("Patient with patientId = " + patientId + " not found."));
        return patient;
    }
}
