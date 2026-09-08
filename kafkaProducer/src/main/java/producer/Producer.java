package producer;

import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Producer {
    public static void main(String[] args) {

        final Logger log = LoggerFactory.getLogger(Producer.class);

//        getProducer() helper method used
        KafkaProducer<String, String> producer = getProducer();

//         Sample message
        String topic = "cron-jobs";
        String key = UUID.randomUUID().toString();
        String messagePayload = "{\"message\": \"Hello World from Pooriya\"}";

        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, messagePayload);

//        Send to Kafka
//        producer.send(record);
        producer.send(record, (metadata, exception) -> {
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