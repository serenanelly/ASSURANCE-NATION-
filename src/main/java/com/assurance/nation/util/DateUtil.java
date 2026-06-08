package com.assurance.nation.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private DateUtil() {}

    public static final DateTimeFormatter REMBOURSEMENT_YEAR = DateTimeFormatter.ofPattern("yyyy");

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
