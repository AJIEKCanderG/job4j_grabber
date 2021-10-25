package date;

import org.junit.Assert;
import org.junit.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Ajiekcander
 */
public class SqlRuDateTimeParserTest {

    @Test
    public void whenDayMonthYear() {
        LocalDateTime fullTimeAndDate = new SqlRuDateTimeParser().parse("24 окт 21, 10:10");
        LocalDateTime expected = LocalDateTime.of(
                LocalDate.of(2021, 10, 24),
                        LocalTime.of(10, 10));
        Assert.assertEquals(expected, fullTimeAndDate);
    }

    @Test
    public void whenToday() {
        LocalDateTime fullTimeAndDate = new SqlRuDateTimeParser().parse("сегодня, 11:11");
        LocalDateTime expected = LocalDateTime
                .of(LocalDate.now(), LocalTime
                        .of(11, 11));
        Assert.assertEquals(expected, fullTimeAndDate);
    }
    @Test
    public void whenYesterday() {
        LocalDateTime fullTimeAndDate = new SqlRuDateTimeParser().parse("вчера, 09:01");
        LocalDateTime expected = LocalDateTime
                .of(LocalDate.now().minusDays(1), LocalTime
                        .of(9, 1));
        Assert.assertEquals(expected, fullTimeAndDate);
    }
}