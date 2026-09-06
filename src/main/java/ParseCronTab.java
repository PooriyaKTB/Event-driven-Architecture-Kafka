import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ParseCronTab {
    public static void startApp(String fileName) {

        try (
                BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNumber = 0;

            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String quartzCron = getQuartzCron(line);

                System.out.println("Line " + lineNumber + " -> Quartz expression: " + quartzCron);

                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("CronTrigger" + lineNumber, "CronGroup")
                        .withSchedule(CronScheduleBuilder.cronSchedule(quartzCron))
                        .build();

                JobDetail job = JobBuilder.newJob(CronScheduleCreator.class)
                        .withIdentity("CronJob" + lineNumber, "CronGroup")
                        .usingJobData("lineNumber", lineNumber)
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

    private static String getQuartzCron(String line) {

        String[] cronParts = line.split("\\s+");
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
}
