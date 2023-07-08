package clinic.services;

import clinic.common.Constants;
import clinic.models.*;
import clinic.models.dto.ScheduleDetailAppointmentDto;
import clinic.models.dto.SetAppointmentDto;
import clinic.config.eventhandler.AfterSaveEvent;
import clinic.services.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    @Autowired
    private ScheduleDetailRepository scheduleDetailRepository;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              ScheduleRepository scheduleRepository,
                              ScheduleDetailRepository scheduleDetailRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.scheduleRepository = scheduleRepository;
        this.scheduleDetailRepository = scheduleDetailRepository;
    }

    public Appointment saveAppointment(SetAppointmentDto setAppointmentDto) {
        Doctor doctor = getAppointmentDoctor(setAppointmentDto.getDoctorId());
        Patient patient = getAppointmentPatient(setAppointmentDto.getPatientId());

        Integer durationMinutes = setAppointmentDto.getDurationMinutes();
        validateDurationInMinutes(durationMinutes, doctor);
        LocalDateTime startDateTime = setAppointmentDto.getStartDateTime();
        validateStartDateTime(startDateTime);
        ScheduleDetail scheduleDetail = getScheduleDetailByDoctor(doctor, startDateTime);
        List<Appointment> patientAppointments = checkPatientAppointmentsCount(startDateTime, patient);
        if (patientAppointments != null && !patientAppointments.isEmpty()) {
            checkPatientAppointmentsOverlap(startDateTime, durationMinutes, patientAppointments, patient.getPatientId());
        }
        checkStartDateTimeInScheduleDetailRange(startDateTime, durationMinutes, scheduleDetail);
        checkDoctorAppointmentsOverlap(doctor, startDateTime, durationMinutes, scheduleDetail);

        Appointment savedAppointment = appointmentRepository.save(
                Appointment.builder()
                        .doctor(doctor)
                        .patient(patient)
                        .durationMinutes(durationMinutes)
                        .startDateTime(startDateTime)
                        .scheduleDetail(scheduleDetail)
                        .build());
        applicationEventPublisher.publishEvent(new AfterSaveEvent(savedAppointment));
        return savedAppointment;
    }

    private void checkStartDateTimeInScheduleDetailRange(LocalDateTime startDateTime,
                                                         Integer durationMinutes,
                                                         ScheduleDetail scheduleDetail) {
        LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
        LocalDateTime scheduleDateTimeStart = scheduleDetail.getScheduleDateTimeStart();
        LocalDateTime scheduleDateTimeEnd = scheduleDetail.getScheduleDateTimeEnd();
        boolean isInRange = ((startDateTime.isAfter(scheduleDateTimeStart) || startDateTime.isEqual(scheduleDateTimeStart))
                && (startDateTime.isBefore(scheduleDateTimeEnd) || startDateTime.isEqual(scheduleDateTimeEnd)))
                &&
                ((endDateTime.isAfter(scheduleDateTimeStart) || endDateTime.isEqual(scheduleDateTimeStart))
                        && (endDateTime.isBefore(scheduleDateTimeEnd) || endDateTime.isEqual(scheduleDateTimeEnd)));
        if (!isInRange) {
            String errorMessage = "Doctor is not present on provided startDateTime: %s." +
                    " Based on startDateTime, doctor is present from %s to %s";
            throw new RuntimeException(
                    String.format(
                            errorMessage,
                            startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            scheduleDateTimeStart.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            scheduleDateTimeEnd.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        }
    }

    public Appointment saveEarliestAppointment(SetAppointmentDto setAppointmentDto) {
        Doctor doctor = getAppointmentDoctor(setAppointmentDto.getDoctorId());
        Patient patient = getAppointmentPatient(setAppointmentDto.getPatientId());
        Integer durationMinutes = setAppointmentDto.getDurationMinutes();
        validateDurationInMinutes(durationMinutes, doctor);
        LocalDateTime startDateTime = getEarliest(doctor, durationMinutes);
        if (startDateTime == null) {
            String errorMsg = "Doctor schedule is full.%s";
            int minDuration = doctor.getDoctorType().getDoctorTypeDurationMin();
            String msg = "";
            if (durationMinutes > minDuration) {
                msg = " You may try smaller durationMinutes. minimum: %d";
                msg = String.format(msg, minDuration);
            }
            throw new RuntimeException(String.format(errorMsg, msg));
        }
        List<Appointment> patientAppointments = checkPatientAppointmentsCount(startDateTime, patient);
        if (patientAppointments != null && !patientAppointments.isEmpty()) {
            checkPatientAppointmentsOverlap(startDateTime, durationMinutes, patientAppointments, patient.getPatientId());
        }
        ScheduleDetail scheduleDetail = getScheduleDetailByDoctor(doctor, startDateTime);
        Appointment savedAppointment = appointmentRepository.save(
                Appointment.builder()
                        .doctor(doctor)
                        .patient(patient)
                        .durationMinutes(durationMinutes)
                        .startDateTime(startDateTime)
                        .scheduleDetail(scheduleDetail)
                        .build());
        applicationEventPublisher.publishEvent(new AfterSaveEvent(savedAppointment));
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

        List<ScheduleDetailAppointmentDto> scheduleDetailAppointmentDtoList =
                scheduleDetailRepository.findAppointmentByScheduleId(schedule.getScheduleId())
                        .orElseThrow(() -> new IllegalArgumentException("No ScheduleDetail found by doctorId : "
                                + doctor.getDoctorId() + " and date greater than or equal today."));

        if (scheduleDetailAppointmentDtoList.isEmpty()) {
            throw new IllegalArgumentException("No ScheduleDetail found by doctorId : "
                    + doctor.getDoctorId() + " and date greater than or equal today.");
        }

        Map<Long, List<Appointment>> scheduleDetailAppointmentsMap = new HashMap<>();
        List<ScheduleDetail> scheduleDetails = new ArrayList<>();
        List<Appointment> allAppointments = new ArrayList<>();
        scheduleDetailAppointmentDtoList.stream().forEach(scheduleDetailAppointmentDto -> {
            ScheduleDetail scheduleDetail = scheduleDetailAppointmentDto.getScheduleDetail();
            Appointment appointment = scheduleDetailAppointmentDto.getAppointment();
            allAppointments.add(appointment);
            scheduleDetails.add(scheduleDetail);
            Long scheduleDetailId = scheduleDetail.getScheduleDetailId();
            List<Appointment> appointments = scheduleDetailAppointmentsMap.get(scheduleDetailId);
            if (appointments == null || appointments.isEmpty()) {
                appointments = new ArrayList<>();
            } else {
                scheduleDetailAppointmentsMap.remove(scheduleDetailId);
            }
            appointments.add(appointment);
            appointments.sort(Comparator.comparing(Appointment::getStartDateTime));
            scheduleDetailAppointmentsMap.put(scheduleDetailId, appointments);
        });
        scheduleDetails.sort(Comparator.comparing(ScheduleDetail::getScheduleDateTimeStart));

        Map<LocalDateTime, List<Long>> startAppointmentIdListMap = new HashMap<>();
        Map<LocalDateTime, List<Long>> endAppointmentIdListMap = new HashMap<>();
        if (!allAppointments.isEmpty()) {
            allAppointments.stream()
                    .filter(Objects::nonNull)
                    .filter(appointment -> appointment.getAppointmentId() != null)
                    .forEach(appointment -> {
                        Long appointmentId = appointment.getAppointmentId();

                        LocalDateTime startDateTime = appointment.getStartDateTime();
                        updateMap(startAppointmentIdListMap, appointmentId, startDateTime);

                        Integer appointmentDurationMinutes = appointment.getDurationMinutes();
                        LocalDateTime startDateTimeEnd = startDateTime.plusMinutes(appointmentDurationMinutes);
                        updateMap(endAppointmentIdListMap, appointmentId, startDateTimeEnd);
                    });
        }

        Integer doctorTypeOverlappingAppointments = doctor.getDoctorType().getDoctorTypeOverlappingAppointments();
        int numberOfAllowedOverlap = doctorTypeOverlappingAppointments > 0 ? doctorTypeOverlappingAppointments : 0;
        Map<Long, Set<Long>> detailIdOverlappingAppointmentsMap = new HashMap<>();
        scheduleDetailAppointmentsMap.keySet().stream()
                .forEach(scheduleDetailId -> {
                    Set<Long> overlappingAppointments =
                            checkOverlappingAppointments(
                                    scheduleDetailAppointmentsMap.get(scheduleDetailId),
                                    numberOfAllowedOverlap);
                    detailIdOverlappingAppointmentsMap.put(scheduleDetailId, overlappingAppointments);
                });

        LocalDateTime appointmentDateTime = null;
        for (Iterator<ScheduleDetail> iterator = scheduleDetails.iterator();
             appointmentDateTime == null && iterator.hasNext(); ) {
            ScheduleDetail detail = iterator.next();
            Long scheduleDetailId = detail.getScheduleDetailId();
            List<Appointment> appointments = scheduleDetailAppointmentsMap.get(scheduleDetailId);
            if (appointments == null || appointments.isEmpty()) {
                appointmentDateTime = detail.getScheduleDateTimeStart();
            }
            if (appointmentDateTime == null) {
                appointmentDateTime = getEarliestDateTime(
                        durationMinutes,
                        startAppointmentIdListMap,
                        endAppointmentIdListMap,
                        numberOfAllowedOverlap,
                        detailIdOverlappingAppointmentsMap,
                        detail,
                        scheduleDetailId);
            }
        }
        return appointmentDateTime;
    }

    private static LocalDateTime getEarliestDateTime(Integer durationMinutes,
                                                     Map<LocalDateTime,
                                                             List<Long>> startAppointmentIdListMap,
                                                     Map<LocalDateTime, List<Long>> endAppointmentIdListMap,
                                                     int numberOfAllowedOverlap,
                                                     Map<Long, Set<Long>> detailIdOverlappingAppointmentsMap,
                                                     ScheduleDetail detail,
                                                     Long scheduleDetailId) {
        LocalDateTime appointmentDateTime = null;
        LocalDateTime detailStart = detail.getScheduleDateTimeStart();
        Set<Long> visitedAppointments = new HashSet<>();
        for (LocalDateTime start = detailStart;
             (start.isBefore(detailStart.plusMinutes(durationMinutes)) || start.isEqual(detailStart.plusMinutes(durationMinutes)));
             start = start.plusMinutes(1)) {
            List<Long> matchedAppointments = startAppointmentIdListMap.get(start);
            if (matchedAppointments != null) {
                visitedAppointments.addAll(matchedAppointments);
            }
        }

        Set<Long> overlappingAppointments = detailIdOverlappingAppointmentsMap.get(scheduleDetailId);
        boolean isOverlapAllowed = overlappingAppointments.size() != numberOfAllowedOverlap;
        Set<Long> visitedAndOverlappingAppointments = new HashSet<>();
        visitedAndOverlappingAppointments.addAll(visitedAppointments);
        visitedAndOverlappingAppointments.addAll(overlappingAppointments);
        if ((isOverlapAllowed && visitedAndOverlappingAppointments.size() < numberOfAllowedOverlap) || (visitedAppointments.size() == 0)) {
            appointmentDateTime = detailStart;
        }

        for (LocalDateTime start = detailStart.plusMinutes(durationMinutes).plusMinutes(1);
             (appointmentDateTime == null && (start.isBefore(detail.getScheduleDateTimeEnd()) || start.isEqual(detail.getScheduleDateTimeEnd())));
             start = start.plusMinutes(1)) {
            LocalDateTime possibleEarliestStartDateTime = start.minusMinutes(durationMinutes);

            List<Long> removableAppointmentIdList = endAppointmentIdListMap.get(possibleEarliestStartDateTime.minusMinutes(1));
            if (removableAppointmentIdList != null) {
                visitedAppointments.removeAll(removableAppointmentIdList);
                removableAppointmentIdList.removeAll(overlappingAppointments);
                visitedAndOverlappingAppointments.removeAll(removableAppointmentIdList);
            }

            List<Long> matchedAppointments = startAppointmentIdListMap.get(start);
            if (matchedAppointments != null) {
                visitedAppointments.addAll(matchedAppointments);
                visitedAndOverlappingAppointments.addAll(matchedAppointments);
            }

            if (visitedAppointments.size() == 0) {
                appointmentDateTime = possibleEarliestStartDateTime;
            } else if (isOverlapAllowed
                    && visitedAndOverlappingAppointments.size() < numberOfAllowedOverlap
            ) {
                appointmentDateTime = possibleEarliestStartDateTime;
            }
        }
        return appointmentDateTime;
    }

    private static void updateMap(Map<LocalDateTime, List<Long>> startEndAppointmentsMap, Long appointmentId, LocalDateTime startDateTime) {
        List<Long> appointmentIdList = startEndAppointmentsMap.get(startDateTime);
        if (appointmentIdList == null) {
            appointmentIdList = new ArrayList<>();
        } else {
            startEndAppointmentsMap.remove(startDateTime);
        }
        appointmentIdList.add(appointmentId);
        startEndAppointmentsMap.put(startDateTime, appointmentIdList);
    }

    private static Set<Long> checkOverlappingAppointments(List<Appointment> appointments,
                                                          int numberOfAllowedOverlap) {
        Appointment previousAppointment = null;
        Set<Long> overlappingAppointments = new HashSet<>();
        for (int i = 0; numberOfAllowedOverlap > 0 && i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            if (appointment != null && appointment.getStartDateTime() != null) {
                LocalDateTime appointmentStart = appointment.getStartDateTime();
                if (previousAppointment != null) {
                    LocalDateTime previousEnd = previousAppointment.getStartDateTime().plusMinutes(previousAppointment.getDurationMinutes());
                    if (isOverlappingAppointment(previousEnd, appointmentStart)) {
                        overlappingAppointments.add(previousAppointment.getAppointmentId());
                        overlappingAppointments.add(appointment.getAppointmentId());
                        if (overlappingAppointments.size() == numberOfAllowedOverlap) {
                            numberOfAllowedOverlap = 0;
                        }
                    }
                }
                previousAppointment = appointment;
            }
        }
        return overlappingAppointments;
    }

    private void validateStartDateTime(LocalDateTime startDateTime) {
        if (startDateTime == null) {
            throw new IllegalArgumentException("startDateTime is null.");
        }
        LocalDateTime today = LocalDateTime.now();
        if (startDateTime.isBefore(today)) {
            throw new RuntimeException("startDateTime is invalid. startDateTime is before today date : "
                    + today.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (startDateTime.getDayOfWeek().getValue() == DayOfWeek.THURSDAY.getValue()
                || startDateTime.getDayOfWeek().getValue() == DayOfWeek.FRIDAY.getValue()) {
            throw new RuntimeException("startDateTime is invalid. Valid Days are between Saturday to Wednesday.");
        }
    }

    private List<Appointment> checkPatientAppointmentsCount(LocalDateTime appointmentDate, Patient patient) {
        List<Appointment> patientAppointments = appointmentRepository.findAppointmentsByPatientAndStartDateTimeEquals(
                patient.getPatientId(),
                appointmentDate.format(DateTimeFormatter.ISO_DATE));
        if (patientAppointments != null
                && !patientAppointments.isEmpty()
                && patientAppointments.size() == Constants.MAXIMUM_NUMBER_OF_PATIENT_APPOINTMENTS_IN_A_DAY) {
            throw new RuntimeException("Patient already reserved "
                    + Constants.MAXIMUM_NUMBER_OF_PATIENT_APPOINTMENTS_IN_A_DAY +
                    " appointments in provided startDateTime : "
                    + appointmentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return patientAppointments;
    }

    private void checkPatientAppointmentsOverlap(LocalDateTime appointmentTime,
                                                 Integer duration,
                                                 List<Appointment> patientAppointments,
                                                 Long patientId) {
        List<Appointment> patientOverlapAppointments = patientAppointments.stream()
                .filter(appointment ->
                        isOverlappingWithStartDateTime(appointmentTime, duration, appointment.getStartDateTime(), appointment.getDurationMinutes())
                ).collect(Collectors.toList());
        if (patientOverlapAppointments != null && !patientOverlapAppointments.isEmpty()) {
            throw new RuntimeException(
                    "Provided patientId : " + patientId + " has already booked appointments" +
                            " which are overlapping with startDateTime : "
                            + appointmentTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private void checkDoctorAppointmentsOverlap(Doctor doctor,
                                                LocalDateTime startDateTime,
                                                Integer durationMinutes,
                                                ScheduleDetail scheduleDetail) {
        List<Appointment> appointments = appointmentRepository.findAppointmentsByScheduleDetail(scheduleDetail);
        if (appointments != null && !appointments.isEmpty()) {
            List<Appointment> overlappingWithStartDateTime =
                    appointments.stream().filter(appointment ->
                                    isOverlappingWithStartDateTime(
                                            startDateTime,
                                            durationMinutes,
                                            appointment.getStartDateTime(),
                                            appointment.getDurationMinutes()))
                            .collect(Collectors.toList());

            if (!overlappingWithStartDateTime.isEmpty()) {
                Integer doctorTypeOverlappingAppointments = doctor.getDoctorType().getDoctorTypeOverlappingAppointments();
                int numberOfAllowedOverlap = doctorTypeOverlappingAppointments > 0 ? doctorTypeOverlappingAppointments : 0;
                Set<Long> overlappingAppointments =
                        checkOverlappingAppointments(
                                appointments,
                                numberOfAllowedOverlap);
                boolean isNotOverlapAllowed = overlappingAppointments.size() == numberOfAllowedOverlap;
                if (isNotOverlapAllowed) {
                    String errorMessage = "No more overlapping is allowed! " +
                            "Appointment overlaps with other booked appointments. doctorId: %d"
                            + Constants.COMMA_SPACE +
                            "startDateTime Start: %s" +
                            Constants.COMMA_SPACE +
                            "startDateTime End: %s" +
                            " Overlapping with previously booked appointments.";
                    throw new RuntimeException(
                            String.format(errorMessage,
                                    doctor.getDoctorId(),
                                    startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                    startDateTime.plusMinutes(durationMinutes).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
                }
            }
        }
    }

    private ScheduleDetail getScheduleDetailByDoctor(Doctor doctor, LocalDateTime startDateTime) {
        Schedule doctorSchedule = checkDoctorSchedule(doctor, startDateTime);
        ScheduleDetail scheduleDetail = getScheduleDetailBySchedule(doctorSchedule, startDateTime);
        return scheduleDetail;
    }

    private ScheduleDetail getScheduleDetailBySchedule(Schedule schedule, LocalDateTime startDateTime) {
        List<ScheduleDetail> scheduleDetails =
                scheduleDetailRepository.
                        findScheduleDetailByScheduleAndScheduleDateTimeStartLessThanEqualAndAndScheduleDateTimeEndGreaterThanEqual(
                                schedule,
                                startDateTime,
                                startDateTime);
        if (scheduleDetails == null || scheduleDetails.isEmpty()) {
            throw new RuntimeException(String.format("No scheduleDetail found by startDateTime: %s ." +
                            "Doctor is not available on provided startDateTime.",
                    startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        }
        if (scheduleDetails.size() > 1) {
            throw new RuntimeException(String.format("Duplicate scheduleDetail found by startDateTime: %s",
                    startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        }
        ScheduleDetail scheduleDetail = scheduleDetails.get(0);
        return scheduleDetail;
    }

    private static boolean isOverlappingWithStartDateTime(LocalDateTime startDateTime,
                                                          Integer durationMinutes,
                                                          LocalDateTime appointmentStartDateTime,
                                                          Integer appointmentDuration) {
        LocalDateTime appointmentEndDateTime = appointmentStartDateTime.plusMinutes(appointmentDuration);
        LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
        return ((startDateTime.isAfter(appointmentStartDateTime) || startDateTime.isEqual(appointmentStartDateTime))
                && (startDateTime.isBefore(appointmentEndDateTime) || startDateTime.isEqual(appointmentEndDateTime)))
                ||
                ((endDateTime.isAfter(appointmentStartDateTime) || endDateTime.isEqual(appointmentStartDateTime))
                        && (endDateTime.isBefore(appointmentEndDateTime) || endDateTime.isEqual(appointmentEndDateTime)))
                ;
    }

    private static boolean isOverlappingAppointment(LocalDateTime previousAppointmentEnd, LocalDateTime appointmentStartDateTime) {
        return previousAppointmentEnd.isAfter(appointmentStartDateTime)
                || previousAppointmentEnd.isEqual(appointmentStartDateTime);
    }

    private Schedule checkDoctorSchedule(Doctor doctor, LocalDateTime startDateTime) {
        List<Schedule> doctorSchedules = scheduleRepository.
                findSchedulesByDoctorAndScheduleEndDateGreaterThanEqualAndScheduleStartDateLessThanEqual(doctor, startDateTime, startDateTime);
        if (doctorSchedules == null || doctorSchedules.isEmpty()) {
            throw new RuntimeException(
                    String.format("No schedule found by doctorId: %d and startDateTime: %s",
                            doctor.getDoctorId(),
                            startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    ));
        }
        if (doctorSchedules.size() > 1) {
            throw new RuntimeException(
                    String.format("More than one schedule is found by doctorId: %d and startDateTime: %s",
                            doctor.getDoctorId(),
                            startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        }
        return doctorSchedules.get(0);
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
