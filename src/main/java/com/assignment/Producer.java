package com.assignment;

import org.apache.kafka.clients.producer.*;
import com.google.gson.Gson;
import java.io.*;
import java.util.*;

public class Producer {

    private static final String TOPIC = "raw-data";
    private static final String BOOTSTRAP = "localhost:9092";

    public static void main(String[] args) throws Exception {

        // Kafka producer config
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP);
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        Gson gson = new Gson();

        // Read CSV
        BufferedReader reader = new BufferedReader(
                new FileReader("data/hour.csv")
        );

        String header = reader.readLine(); // skip header
        String[] columns = header.split(",");

        String line;
        int rowNum = 0;

        System.out.println("Producer started. Sending rows to '" + TOPIC + "'...");

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");

            // Build a map of column -> value
            Map<String, String> record = new LinkedHashMap<>();
            for (int i = 0; i < columns.length; i++) {
                record.put(columns[i].trim(), values[i].trim());
            }

            String json = gson.toJson(record);

            // Send to Kafka
            producer.send(new ProducerRecord<>(TOPIC,
                    String.valueOf(rowNum), json));

            System.out.println("Sent row " + rowNum + ": " + json);

            rowNum++;
            Thread.sleep(1000); // 1 row per second
        }

        reader.close();
        producer.close();
        System.out.println("Producer finished.");
    }
}