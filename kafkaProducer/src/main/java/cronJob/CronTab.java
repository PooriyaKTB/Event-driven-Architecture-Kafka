package cronJob;

public class CronTab {

    public static String getQuartzCron(String line) {

        String[] cronParts = line.split("\\s+", 6);

        if (cronParts.length < 5) {
            throw new IllegalArgumentException(
                    "Expected 5 schedule fields (minute hour day-of-month month day-of-week) but found "
                            + cronParts.length + " in: " + line);
        }

        String min = cronParts[0];
        String hour = cronParts[1];
        String dayOfMonth = cronParts[2];
        String month = cronParts[3];
        String dayOfWeek = cronParts[4];

        if (dayOfWeek.equals("*")) {
            dayOfWeek = "?";
        } else if (dayOfMonth.equals("*")) {
            dayOfMonth = "?";
        }

        return "0 " + min + " " + hour + " " + dayOfMonth + " " + month + " " + dayOfWeek;
    }

    static String getCommand(String line) {

        String[] cronParts = line.split("\\s+", 6);
        return cronParts.length == 6 ? cronParts[5] : "";
    }
}
