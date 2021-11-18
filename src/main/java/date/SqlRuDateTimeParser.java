package date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.Map.entry;

/**
 * @author Ajiekcander
 */
public class SqlRuDateTimeParser implements DateTimeParser {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d M yy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, String> MONTHS = Map.ofEntries(
            entry("янв", "1"),
            entry("фев", "2"),
            entry("мар", "3"),
            entry("апр", "4"),
            entry("май", "5"),
            entry("июн", "6"),
            entry("июл", "7"),
            entry("авг", "8"),
            entry("сен", "9"),
            entry("окт", "10"),
            entry("ноя", "11"),
            entry("дек", "12")
            );

    @Override
    public LocalDateTime parserTimeAndDate(String parse) {
        String[] dates = parse.split(",");
        String[] date = dates[0].split(" ");
        LocalDate day;
        LocalTime time = LocalTime.parse(dates[1].trim(), TIME_FORMATTER);
        if (date.length == 3) {
           day = LocalDate.parse(String.format("%s %s %s", date[0], MONTHS.get(date[1]), date[2]),
                    DATE_FORMATTER);
        } else if (date[0].contains("сегодня")) {
           day =  LocalDate.now();
        }  else if (date[0].contains("вчера")) {
           day = LocalDate.now().minusDays(1);
        } else {
            throw new IllegalArgumentException("Illegal date");
        }
        return LocalDateTime.of(day, time);
    }

    public static void main(String[] args) {
        SqlRuDateTimeParser parser = new SqlRuDateTimeParser();
        String a = "22 янв 16, 10:56";
        String b = "сегодня, 10:37";
        String c = "вчера, 20:33";
        System.out.println(parser.parserTimeAndDate(a));
        System.out.println(parser.parserTimeAndDate(b));
        System.out.println(parser.parserTimeAndDate(c));
    }
}
