package cronJob;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JobScheduler {

    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);
    private static final String GROUP = "CronGroup";

    public void startApp(String fileName) throws SchedulerException, IOException {

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        try (
                BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int jobId = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                schedule(scheduler, jobId, line);
                jobId++;
            }

            scheduler.start();
        }
    }

    private static void schedule(Scheduler scheduler, int jobId, String line) throws SchedulerException {

        String quartzCron = CronTab.getQuartzCron(line);
        String command = CronTab.getCommand(line);

        JobDetail job = JobBuilder.newJob(CronJob.class)
                .withIdentity("CronJob" + jobId, GROUP)
                .usingJobData(CronJob.JOB_ID, jobId)
                .usingJobData(CronJob.COMMAND, command)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("CronTrigger" + jobId, GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(quartzCron))
                .build();

        scheduler.scheduleJob(job, trigger);

        log.info("Scheduled job {} [{}] -> {}", jobId, quartzCron, command);
    }
}
