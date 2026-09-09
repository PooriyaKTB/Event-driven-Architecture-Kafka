package cronJob;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronJob implements Job {

    static final String JOB_ID = "jobId";
    static final String COMMAND = "command";
    static final String CRON_EXPRESSION = "cronExpression";

    private static final Logger log = LoggerFactory.getLogger(CronJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getMergedJobDataMap();

        CronJobMessage message = new CronJobMessage(
                dataMap.getInt(JOB_ID),
                dataMap.getString(COMMAND),
                dataMap.getString(CRON_EXPRESSION),
                context.getScheduledFireTime().toInstant());

        try {
            JobPublisher publisher = (JobPublisher) context.getScheduler()
                    .getContext()
                    .get(JobPublisher.CONTEXT_KEY);

            publisher.publishJob(message);
        } catch (SchedulerException e) {
            throw new JobExecutionException("Could not read the publisher from the scheduler context", e);
        }


        log.info("Queued job {}: {}", message.jobId(), message.command());
    }
}
