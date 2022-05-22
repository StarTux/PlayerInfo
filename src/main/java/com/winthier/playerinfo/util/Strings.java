package com.winthier.playerinfo.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public final class Strings {
    private static final int DAYS_PER_YEAR = 365;
    private static final int DAYS_PER_MONTH = 30;
    private static final DateFormat FORMAT_DATE_FORMAT = new SimpleDateFormat("MMM dd yyyy HH:mm:ss");
    private static final Pattern IP_PATTERN = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");

    public static String formatDate(Date date) {
        return FORMAT_DATE_FORMAT.format(date);
    }

    public static String formatSeconds(final long seconds) {
        final long minutes = seconds / 60;
        final long hours = minutes / 60;
        final long days = hours / 24;
        if (days == 0L) {
            return String.format("%02d:%02d:%02d", hours % 24, minutes % 60, seconds % 60);
        }
        final long years = days / DAYS_PER_YEAR;
        final long months = Math.min(12, (days - years * DAYS_PER_YEAR) / DAYS_PER_MONTH);
        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append("y");
        if (months > 0) sb.append(months).append("m");
        if (days > 0) sb.append(days % DAYS_PER_MONTH).append("d");
        sb.append(String.format(" %02d:%02d", hours % 24, minutes % 60));
        return sb.toString();
    }

    public static String formatTimeDiff(Date from, Date to) {
        if (from == null) throw new NullPointerException("Date from cannot be null");
        if (from == null) throw new NullPointerException("Date to cannot be null");
        final long dist = Math.abs(to.getTime() - from.getTime());
        final long seconds = dist / 1000;
        return formatSeconds(seconds);
    }

    public static String formatTimeDiffToNow(Date from) {
        return formatTimeDiff(from, new Date());
    }

    public static String camelCase(String name) {
        String[] tokens = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append(token.substring(0, 1).toUpperCase());
            sb.append(token.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public static String join(List<String> list, String delim) {
        if (list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(list.get(0));
        for (int i = 1; i < list.size(); ++i) {
            sb.append(delim);
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    public static boolean isIP(String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    private Strings() { }
}
