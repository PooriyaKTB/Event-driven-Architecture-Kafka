package cronJob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class JobPublisher implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(JobPublisher.class);

    public static final String CONTEXT_KEY = "jobPublisher";

    private static final String TOPIC = "cron-jobs";
    private static final int PARTITIONS = 1;
    private static final short REPLICATION_FACTOR = 1;

    private static final String BOOTSTRAP_SERVERS =
            System.getenv().getOrDefault("KAFKA_BROKER", "localhost:9092");

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final KafkaProducer<String, String> producer;

    public JobPublisher() {
        createTopic();
        this.producer = getProducer();
    }

    public void publishJob(CronJobMessage message) {

        String messagePayload;
        try {
            messagePayload = mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise message for job " + message.jobId(), e);
        }

        String key = UUID.randomUUID().toString();
        ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(TOPIC, key, messagePayload);

        producer.send(kafkaRecord, (metadata, exception) -> {
            if (exception == null) {
                log.info("Published job {} to partition {} at offset {}",
                        message.jobId(), metadata.partition(), metadata.offset());
            } else {
                log.error("Failed to publish job {}", message.jobId(), exception);
            }
        });
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }

    private void createTopic() {

        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (Admin admin = Admin.create(config)) {
            admin.createTopics(List.of(new NewTopic(TOPIC, PARTITIONS, REPLICATION_FACTOR)))
                    .all()
                    .get();

            log.info("Created topic {} with {} partitions", TOPIC, PARTITIONS);

        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                log.info("Topic {} already exists, leaving it as it is", TOPIC);
            } else {
                throw new IllegalStateException("Could not create topic " + TOPIC, e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while creating topic " + TOPIC, e);
        }
    }

    private KafkaProducer<String, String> getProducer() {

        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(config);
    }
}