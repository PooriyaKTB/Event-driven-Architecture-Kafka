package cronJob;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronJob implements Job {

    static final String JOB_ID = "jobId";
    static final String COMMAND = "command";

    private static final Logger log = LoggerFactory.getLogger(CronJob.class);


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getMergedJobDataMap();

        int jobId = dataMap.getInt(JOB_ID);
        String command = dataMap.getString(COMMAND);

        log.info("Running job {}: {}", jobId, command);
    }
}
