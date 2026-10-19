package cronJob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

public class Consumer {

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    private static final String TOPIC = "cron-jobs";
    private static final String GROUP_ID = "cron-app";

    private static final String BOOTSTRAP_SERVERS =
            System.getenv().getOrDefault("KAFKA_BROKER", "localhost:9092");

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static void main(String[] args) {

        CommandRunner runner = new CommandRunner();

        try (KafkaConsumer<String, String> consumer = getConsumer()) {

            consumer.subscribe(List.of(TOPIC));

            while (true) {
                ConsumerRecords<String, String> kafkaRecords = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> kafkaRecord : kafkaRecords) {
                    handle(kafkaRecord, runner);
                }
            }
        }
    }

    private static void handle(ConsumerRecord<String, String> kafkaRecord, CommandRunner runner) {

        CronJobMessage message;
        try {
            message = mapper.readValue(kafkaRecord.value(), CronJobMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Skipping malformed message at offset {}: {}",
                    kafkaRecord.offset(), kafkaRecord.value(), e);
            return;
        }

        long waitedMillis = Duration.between(message.scheduledAt(), Instant.now()).toMillis();

        log.info("Received job {} from partition {} at offset {}, waited {} ms in the queue",
                message.jobId(), kafkaRecord.partition(), kafkaRecord.offset(), waitedMillis);

        runner.run(message);
    }

    private static KafkaConsumer<String, String> getConsumer() {

        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaConsumer<>(config);
    }
}