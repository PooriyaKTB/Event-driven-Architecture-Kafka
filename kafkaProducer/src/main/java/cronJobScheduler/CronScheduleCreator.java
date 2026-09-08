package cronJobScheduler;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronScheduleCreator implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Logger log = LoggerFactory.getLogger(CronScheduleCreator.class);

        JobDataMap dataMap = context.getMergedJobDataMap();
        int lineNumber = dataMap.getInt("lineNumber");
        String command = dataMap.getString("command");
        log.info("Running job " + lineNumber + " " + command);
    }
}
