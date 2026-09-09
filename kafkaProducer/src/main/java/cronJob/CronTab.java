package cronJob;

public class CronTab {

    public static String getQuartzCron(String line) {

        String[] parts = splitLine(line);

        String min = parts[0];
        String hour = parts[1];
        String dayOfMonth = parts[2];
        String month = parts[3];
        String dayOfWeek = parts[4];

        if (dayOfWeek.equals("*")) {
            dayOfWeek = "?";
        } else if (dayOfMonth.equals("*")) {
            dayOfMonth = "?";
        }

        return "0 " + min + " " + hour + " " + dayOfMonth + " " + month + " " + dayOfWeek;
    }

    static String getCommand(String line) {

        String[] parts = splitLine(line);
        return parts.length == 6 ? parts[5] : "";
    }

    static String getSchedule(String line) {
        String[] parts = splitLine(line);
        return String.join(" ", parts[0], parts[1], parts[2], parts[3], parts[4]);
    }

    private static String[] splitLine(String line) {
        String[] parts = line.split("\\s+", 6);

        if (parts.length < 5) {
            throw new IllegalArgumentException(
                    "Expected 5 schedule fields (minute hour day-of-month month day-of-week) but found "
                            + parts.length + " in: " + line);
        }

        return parts;
    }
}
