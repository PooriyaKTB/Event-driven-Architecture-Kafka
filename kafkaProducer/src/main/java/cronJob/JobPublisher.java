package cronJob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;

public class JobPublisher implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(JobPublisher.class);

    public static final String CONTEXT_KEY = "jobPublisher";
    private static final String TOPIC  = "cron-jobs";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private final KafkaProducer<String, String> producer = getProducer();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    public void publishJob(CronJobMessage message) {
        String messagePayload;

        try {
            messagePayload = mapper.writeValueAsString(message);
        } catch(JsonProcessingException e){
            throw new IllegalStateException("Could not serialise message for job " + message.jobId(), e);
        }

        String key = UUID.randomUUID().toString();

        final ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(TOPIC, key, messagePayload);

//        Send to Kafka
        producer.send(kafkaRecord, (metadata, exception) -> {
            if (exception == null) {
                log.info("Published job {} to partition {} at offset {}", message.jobId(), metadata.partition(), metadata.offset());
            } else {
                log.error("Failed to publish job {}", message.jobId(), exception);
            }
        });
    }

    private KafkaProducer<String, String> getProducer() {

        Properties config = new Properties();

//        Where is kafka? on local host port 9092, for initial connection (Initial Contact Point) and getting Cluster metadata
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

//        Serialising data
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

//        Create producer base on config
        return new KafkaProducer<>(config);
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}
