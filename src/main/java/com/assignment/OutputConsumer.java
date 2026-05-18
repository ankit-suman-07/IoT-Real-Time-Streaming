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

                try {
                    System.out.println("\nRAW MESSAGE:");
                    System.out.println(record.value());

                    JsonObject parsed;
                    try {
                        parsed = gson.fromJson(record.value(), JsonObject.class);
                    } catch (Exception e) {
                        System.out.println("Skipping invalid JSON");
                        continue;
                    }

                    // ---- SAFE INPUT ----
                    JsonObject inputObj = parsed.has("input")
                            ? parsed.getAsJsonObject("input")
                            : null;

                    if (inputObj == null || !inputObj.has("hr")) {
                        System.out.println("Skipping: missing input/hr");
                        continue;
                    }

                    double hr = inputObj.get("hr").getAsDouble();

                    // ---- SAFE PREDICTION ----
                    JsonObject predictionObj = parsed.has("prediction")
                            ? parsed.getAsJsonObject("prediction")
                            : null;

                    if (predictionObj == null) {
                        System.out.println("Skipping: missing prediction");
                        continue;
                    }

                    Double predicted = null;

                    if (predictionObj.has("predicted_rentals")) {
                        predicted = predictionObj.get("predicted_rentals").getAsDouble();

                    } else if (predictionObj.has("prediction")) {
                        predicted = predictionObj.get("prediction").getAsDouble();

                    } else if (predictionObj.has("predictions")) {
                        predicted = predictionObj.get("predictions").getAsDouble();
                    }

                    if (predicted == null) {
                        System.out.println("Unknown prediction format: " + predictionObj);
                        continue;
                    }

                    System.out.println("─────────────────────────────");
                    System.out.println("Hour:              " + (int) hr);
                    System.out.println("Predicted rentals: " + predicted);
                    System.out.println("─────────────────────────────\n");

                } catch (Exception e) {
                    System.out.println("ERROR processing record:");
                    System.out.println(record.value());
                    e.printStackTrace();
                }
            }
        }
    }
}