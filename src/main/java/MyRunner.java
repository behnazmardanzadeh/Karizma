import clinic.models.Appointment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MyRunner {
    public static void main(String[] args) {
        String today = "2023-07-01T09:00";
        LocalDate todayDate  = LocalDate.parse(today, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//        LocalDate today = LocalDate.now();
//
//        String myDate = today.format(DateTimeFormatter.ISO_DATE);
        List<Appointment> appointments = new ArrayList<>();
        String date = "2023-07-01T10:15";
        LocalDateTime date1  = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Appointment appointment = new Appointment();
        appointment.setStartDateTime(date1);
        appointments.add(appointment);
        Appointment appointment1 = new Appointment();
        LocalDateTime date2  = LocalDateTime.parse("2023-07-02T09:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        appointment1.setStartDateTime(date2);
        appointments.add(appointment1);
        Appointment appointment2 = new Appointment();
        LocalDateTime date3  = LocalDateTime.parse("2023-07-01T10:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        appointment2.setStartDateTime(date3);
        appointments.add(appointment2);

        List<Appointment> doctorAppointments = appointments.stream()
                .sorted(Comparator.comparing(Appointment::getStartDateTime))
                .collect(Collectors.toList());

        doctorAppointments.stream().forEach(appointment3 -> System.out.println(appointment3.getStartDateTime()));
    }
}
