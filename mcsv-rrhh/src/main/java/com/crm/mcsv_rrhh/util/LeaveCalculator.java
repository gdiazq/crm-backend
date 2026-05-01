package com.crm.mcsv_rrhh.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class LeaveCalculator {

    private LeaveCalculator() {}

    public static BigDecimal computeTotalDays(LocalDate startDate, LocalDate endDate, Boolean halfDay) {
        if (Boolean.TRUE.equals(halfDay)) {
            return BigDecimal.valueOf(0.5);
        }
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return BigDecimal.valueOf(totalDays);
    }
}
