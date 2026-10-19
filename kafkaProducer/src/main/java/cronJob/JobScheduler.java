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
    private final JobPublisher publisher;

    public JobScheduler(JobPublisher publisher) {
        this.publisher = publisher;
    }

    public void startApp(String fileName) throws SchedulerException, IOException {

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.getContext().put(JobPublisher.CONTEXT_KEY, publisher);

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
        String schedule = CronTab.getSchedule(line);


        JobDetail job = JobBuilder.newJob(CronJob.class)
                .withIdentity("CronJob-" + jobId, GROUP)
                .usingJobData(CronJob.JOB_ID, jobId)
                .usingJobData(CronJob.COMMAND, command)
                .usingJobData(CronJob.CRON_EXPRESSION, schedule)

                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("CronTrigger-" + jobId, GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(quartzCron))
                .build();

        scheduler.scheduleJob(job, trigger);

        log.info("Scheduled job {} [{}] -> {}", jobId, quartzCron, command);
    }
}
