package cronJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String CRONTAB_FILE = "crontab.txt";


    public static void main(String[] args) {
        JobPublisher jobPublisher = new JobPublisher();

        try {
            new JobScheduler(jobPublisher).startApp(CRONTAB_FILE);
        } catch (Exception e) {
            // Top level handler: the user gets one clear line instead of a stack trace.
            log.error("Could not start the scheduler: {}", e.getMessage());
            System.exit(1);
        }
    }
}
