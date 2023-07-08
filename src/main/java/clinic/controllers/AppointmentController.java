package clinic.controllers;

import clinic.common.Routes;
import clinic.models.Appointment;
import clinic.models.dto.SetAppointmentDto;
import clinic.services.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping(path = Routes.POST_api_setAppointment, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Appointment> setAppointment(@RequestBody SetAppointmentDto setAppointmentDto) {
        try {
            Appointment savedAppointment = appointmentService.saveAppointment(setAppointmentDto);
            return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            if (e instanceof  IllegalArgumentException) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping(path = Routes.POST_api_setEarliestAppointment, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Appointment> setEarliestAppointment(@RequestBody SetAppointmentDto setAppointmentDto) {
        try {
            Appointment savedAppointment = appointmentService.saveEarliestAppointment(setAppointmentDto);
            return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof  IllegalArgumentException) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
