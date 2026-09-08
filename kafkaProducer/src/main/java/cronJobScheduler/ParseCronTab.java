package cronJobScheduler;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ParseCronTab {
    public static void startApp(String fileName) {

        Logger log = LoggerFactory.getLogger(ParseCronTab.class);

        try (
                BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;

            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String quartzCron = getQuartzCron(line);

                log.info("Line " + lineNumber + " -> Quartz expression: " + quartzCron);

                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("CronTrigger" + lineNumber, "CronGroup")
                        .withSchedule(CronScheduleBuilder.cronSchedule(quartzCron))
                        .build();

                JobDetail job = JobBuilder.newJob(CronScheduleCreator.class)
                        .withIdentity("CronJob" + lineNumber, "CronGroup")
                        .usingJobData("lineNumber", lineNumber)
                        .usingJobData("command", getCommand(line))
                        .build();

                scheduler.scheduleJob(job, trigger);

                lineNumber++;
            }

            scheduler.start();

        } catch (IOException |
                 SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

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
