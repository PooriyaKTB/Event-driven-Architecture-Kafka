import org.quartz.*;

public class CronScheduleCreator implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getMergedJobDataMap();
        int lineNumber = dataMap.getInt("lineNumber");
        System.out.println("Running job "+ lineNumber);
    }
}
