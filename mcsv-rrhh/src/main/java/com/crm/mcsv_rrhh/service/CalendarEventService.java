package com.crm.mcsv_rrhh.service;

import com.crm.mcsv_rrhh.dto.CalendarEventsResponse;

import java.time.LocalDate;

public interface CalendarEventService {

    CalendarEventsResponse findEvents(LocalDate from,
                                      LocalDate to,
                                      String module,
                                      Long employeeId,
                                      Integer costCenter,
                                      String status);
}
