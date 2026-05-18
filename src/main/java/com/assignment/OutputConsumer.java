package com.assignment;

import org.apache.kafka.clients.consumer.*;
import com.google.gson.*;
import java.time.Duration;
import java.util.*;

public class OutputConsumer {

    private static final String TOPIC     = "predictions";
    private static final String BOOTSTRAP = "localhost:9092";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP);
        props.put("group.id", "output-consumer-group");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        System.out.println("Output Consumer started.");
        System.out.println("Waiting for predictions on '" + TOPIC + "'...\n");

        while (true) {
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                JsonObject parsed = gson.fromJson(
                        record.value(), JsonObject.class
                );

                // Print cleanly
                double hr = parsed.get("input")
                        .getAsJsonObject().get("hr").getAsDouble();
                double predicted = parsed.get("prediction")
                        .getAsJsonObject()
                        .get("predicted_rentals").getAsDouble();

                System.out.println("─────────────────────────────");
                System.out.println("Hour:              " + (int)hr);
                System.out.println("Predicted rentals: " + predicted);
                System.out.println("─────────────────────────────\n");
            }
        }
    }
}