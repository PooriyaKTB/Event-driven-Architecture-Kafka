import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParseCronTabTest {

    // --- Converting a standard cron line into a Quartz expression ---

    @Test
    void addsTheSecondsFieldQuartzRequires() {
        assertEquals("0 * * * * ?", ParseCronTab.getQuartzCron("* * * * *"));
        assertEquals("0 15 * * * ?", ParseCronTab.getQuartzCron("15 * * * *"));
        assertEquals("0 0 1 * * ?", ParseCronTab.getQuartzCron("0 1 * * *"));
    }

    @Test
    void keepsStepValues() {
        assertEquals("0 */5 * * * ?", ParseCronTab.getQuartzCron("*/5 * * * *"));
    }

    @Test
    void replacesTheDayOfWeekWithAQuestionMarkWhenItIsAStar() {
        // Quartz does not allow a value in both the day-of-month and the day-of-week field,
        // so one of them always has to become "?".
        assertEquals("0 0 3 1 * ?", ParseCronTab.getQuartzCron("0 3 1 * *"));
    }

    @Test
    void replacesTheDayOfMonthWithAQuestionMarkWhenADayOfWeekIsGiven() {
        assertEquals("0 0 9 ? * 1", ParseCronTab.getQuartzCron("0 9 * * 1"));
        assertEquals("0 30 8 ? * 1-5", ParseCronTab.getQuartzCron("30 8 * * 1-5"));
        assertEquals("0 0 12 ? * 1,3,5", ParseCronTab.getQuartzCron("0 12 * * 1,3,5"));
    }

    @Test
    void leavesDayNamesAlone() {
        assertEquals("0 0 12 ? * MON-FRI", ParseCronTab.getQuartzCron("0 12 * * MON-FRI"));
    }

    @Test
    void acceptsTabsBetweenFields() {
        assertEquals("0 0 9 * * ?", ParseCronTab.getQuartzCron("0\t9\t*\t*\t*"));
    }

    // --- The command after the schedule ---

    @Test
    void ignoresEverythingAfterTheFiveScheduleFields() {
        // The command may contain spaces, so only the first five fields are the schedule.
        assertEquals("0 0 * * * ?",
                ParseCronTab.getQuartzCron("0 * * * * System.out.println(\"first job\")"));
        assertEquals("0 10 * * * ?",
                ParseCronTab.getQuartzCron("10 * * * * echo hello world"));
    }

    // --- Invalid input ---

    @Test
    void rejectsALineWithTooFewFields() {
        assertThrows(IllegalArgumentException.class,
                () -> ParseCronTab.getQuartzCron("* * * *"));
    }

    @Test
    void rejectsALineThatIsNotACronExpressionAtAll() {
        assertThrows(IllegalArgumentException.class,
                () -> ParseCronTab.getQuartzCron("hello"));
    }

    // --- Known limitations, recorded on purpose ---

    @Test
    void doesNotYetTranslateDayOfWeekNumbering() {
        // UNIX numbers the days 0-6 from Sunday, Quartz numbers them 1-7 from Sunday,
        // so this expression currently runs on Sunday rather than on Monday.
        assertEquals("0 0 9 ? * 1", ParseCronTab.getQuartzCron("0 9 * * 1"));
    }

    @Test
    void doesNotYetRejectADayOfMonthAndADayOfWeekTogether() {
        // Quartz will refuse this expression later, when the trigger is built.
        assertEquals("0 0 3 1 * 1", ParseCronTab.getQuartzCron("0 3 1 * 1"));
    }
}