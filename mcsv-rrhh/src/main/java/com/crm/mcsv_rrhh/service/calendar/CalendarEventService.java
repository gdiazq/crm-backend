package com.crm.mcsv_rrhh.service.calendar;

import com.crm.mcsv_rrhh.dto.calendar.CalendarEventsResponse;

import java.time.LocalDate;

public interface CalendarEventService {

    CalendarEventsResponse findEvents(LocalDate from,
                                      LocalDate to,
                                      String module,
                                      Long employeeId,
                                      Integer costCenter,
                                      String status);
}
