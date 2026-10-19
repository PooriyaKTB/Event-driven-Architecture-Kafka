package cronJob;

import java.time.Instant;

public record CronJobMessage(int jobId, String command, String cronExpression, Instant scheduledAt) {
}
