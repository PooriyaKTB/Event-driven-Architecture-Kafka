package cronJob;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer {
    public static void main(String[] args) {

        Logger log = LoggerFactory.getLogger(Consumer.class);

        try (KafkaConsumer<String, String> consumer = getConsumer()) {

            String topic = "cron-jobs";

            consumer.subscribe(Collections.singleton(topic));

            while (true) {
                ConsumerRecords<String, String> kafkaRecords = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : kafkaRecords) {
//                    log.info(String.valueOf(record));
                    log.info("Received -> Key: {}, Partition: {}, Offset: {}, Value: {}", record.key(), record.partition(), record.offset(), record.value());
                }
            }
        }
    }

    public static KafkaConsumer<String, String> getConsumer() {

        Properties config = new Properties();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "cron-app");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaConsumer<>(config);
    }
}
