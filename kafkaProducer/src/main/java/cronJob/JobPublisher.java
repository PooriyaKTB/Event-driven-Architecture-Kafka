package cronJob;

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

public class JobPublisher {

    private static final Logger log = LoggerFactory.getLogger(JobPublisher.class);

    public static void publishJob() {

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

//        getProducer() helper method used
        KafkaProducer<String, String> producer = getProducer();

//         Sample message
        String topic = "cron-jobs";
        String key = UUID.randomUUID().toString();
        String messagePayload = "{\"message\": \"Hello World from Pooriya\"}";

        final ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(topic, key, messagePayload);

//        Send to Kafka
//        producer.send(record);
        producer.send(kafkaRecord, (metadata, exception) -> {
            if (exception == null) {
                log.info("Sent! Partition: {}, Offset: {}", metadata.partition(), metadata.offset());
            } else {
                log.error("Error sending message: " + exception.getMessage());
            }
        });

//        Close the connection when it's done
        producer.close();
        log.info("Message sent successfully!");
    }

    private static KafkaProducer<String, String> getProducer() {

        Properties config = new Properties();

//        Where is kafka? on local host port 9092, for initial connection (Initial Contact Point) and getting Cluster metadata
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

//        Serialising data
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

//        Create producer base on config
        return new KafkaProducer<>(config);
    }

}
