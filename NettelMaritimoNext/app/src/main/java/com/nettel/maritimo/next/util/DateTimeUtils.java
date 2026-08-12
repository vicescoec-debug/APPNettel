package com.nettel.maritimo.next.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeUtils {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone ECUADOR = TimeZone.getTimeZone("GMT-05:00");

    private DateTimeUtils() {
    }

    public static String utcToEcuador(String value) {
        Date date = parseUtc(value);
        if (date == null) return value == null || value.trim().isEmpty() ? "N/D" : value.trim();
        SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        out.setTimeZone(ECUADOR);
        return out.format(date);
    }

    public static long utcMillis(String value) {
        Date date = parseUtc(value);
        return date == null ? 0 : date.getTime();
    }

    private static Date parseUtc(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String v = value.trim();
        String[] patterns = new String[] {
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                format.setLenient(false);
                format.setTimeZone(UTC);
                return format.parse(v);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
