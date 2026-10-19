package cronJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Runs the command line of a job as a separate operating system process and logs its output.
 */
public class CommandRunner {

    private static final Logger log = LoggerFactory.getLogger(CommandRunner.class);

    public void run(CronJobMessage message) {

        log.info("Running job {}: {}", message.jobId(), message.command());

        try {
            Process process = new ProcessBuilder(message.command().split("\\s+"))
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader output = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = output.readLine()) != null) {
                    log.info("Job {} output: {}", message.jobId(), line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Job {} finished successfully", message.jobId());
            } else {
                log.warn("Job {} failed with exit code {}", message.jobId(), exitCode);
            }

        } catch (IOException e) {
            log.error("Could not start job {}", message.jobId(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for job {}", message.jobId(), e);
        }
    }
}