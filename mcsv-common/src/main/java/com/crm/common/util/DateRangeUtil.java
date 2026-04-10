package com.crm.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DateRangeUtil {

    private DateRangeUtil() {}

    public static LocalDateTime startOf(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    public static LocalDateTime endOf(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
    }
}
