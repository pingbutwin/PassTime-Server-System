package zad1;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class Time {
    private static final List<DateTimeFormatter> formatters = Arrays.asList(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    );
    public static String passed(String from, String to) {
        LocalDateTime dateFrom = parseHelper(from);
        LocalDateTime dateTo = parseHelper(to);
        if(dateFrom == null || dateTo == null)
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("Od ");
        sb = createTitle(dateFrom, sb);
        sb.append(" do ");
        sb = createTitle(dateTo, sb);

        long totalDays = ChronoUnit.DAYS.between(dateFrom, dateTo);
        double totalWeeks = totalDays / 7.0;

        ZonedDateTime zonedFrom = dateFrom.atZone(ZoneId.of("Europe/Warsaw"));
        ZonedDateTime zonedTo = dateTo.atZone(ZoneId.of("Europe/Warsaw"));

        long totalHours = ChronoUnit.HOURS.between(zonedFrom, zonedTo);
        long totalMinutes = ChronoUnit.MINUTES.between(zonedFrom, zonedTo);

        Period calendarPeriod = Period.between(dateFrom.toLocalDate(), dateTo.toLocalDate());
        String calendarDuration = formatCalendarDuration(calendarPeriod);

        sb.append(String.format("\n - mija: %d %s, tygodni %.2f%n",
                totalDays, getPolishDayWord(totalDays), totalWeeks));

        if (from.contains("T") || to.contains("T")) {
            sb.append(String.format(" - godzin: %d, minut: %d%n",
                    totalHours, totalMinutes));
        }

        sb.append(" - kalendarzowo: ").append(calendarDuration);


        return sb.toString();
    }

    private static LocalDateTime parseHelper(String s) {
        for (int i = 0; i < formatters.size(); i++) {
            try {
                if (formatters.get(i).toString().contains("T")) {
                    return LocalDateTime.parse(s, formatters.get(i));
                } else {
                    LocalDate date = LocalDate.parse(s, formatters.get(i));
                    return date.atStartOfDay();
                }
            } catch (DateTimeParseException e) {
                if(i == formatters.size() - 1)
                    System.out.print("*** " + e);
            }
        }
        return null;
    }

    private static Locale findPoland() {
        return Arrays.stream(Locale.getAvailableLocales())
                .sequential().filter(e -> e.getDisplayCountry(Locale.ENGLISH)
                        .equalsIgnoreCase("Poland")).findFirst().orElse(null);
    }

    private static StringBuilder createTitle(LocalDateTime date, StringBuilder sb) {
        Locale polska = findPoland();
        sb.append(date.getDayOfMonth() + " " +
                date.getMonth().getDisplayName(TextStyle.FULL, polska) + " " + date.getYear()
                + " (" + date.getDayOfWeek().getDisplayName(TextStyle.FULL, polska) + ")"
        );
        String hours = String.format("%02d:%02d", date.getHour(), date.getMinute());
        if(!hours.equals("00:00"))
            sb.append(" godz. " + hours);
        return sb;
    }

    private static String getPolishYearWord(long years) {
        return years == 1 ? "rok" : (years < 5 ? "lata" : "lat");
    }

    private static String getPolishMonthWord(long months) {
        return months == 1 ? "miesiąc" : (months < 5 ? "miesiące" : "miesięcy");
    }

    private static String getPolishDayWord(long days) {
        return days == 1 ? "dzień" : "dni";
    }

    private static String formatCalendarDuration(Period period) {
        List<String> parts = new ArrayList<>();

        if (period.getYears() > 0) {
            parts.add(period.getYears() + " " + getPolishYearWord(period.getYears()));
        }
        if (period.getMonths() > 0) {
            parts.add(period.getMonths() + " " + getPolishMonthWord(period.getMonths()));
        }
        if (period.getDays() > 0 || parts.isEmpty()) {
            parts.add(period.getDays() + " " + getPolishDayWord(period.getDays()));
        }

        return String.join(", ", parts);
    }
}

