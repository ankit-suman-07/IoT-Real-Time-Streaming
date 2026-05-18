package com.assignment;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import com.google.gson.*;
import java.net.http.*;
import java.net.URI;
import java.util.Properties;

public class StreamsProcessor {

    private static final String INPUT_TOPIC  = "raw-data";
    private static final String OUTPUT_TOPIC = "predictions";
    private static final String BOOTSTRAP    = "localhost:9092";
    private static final String MODEL_URL    = "http://localhost:5001/predict";

    public static void main(String[] args) {

        // Kafka Streams config
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,
                "bike-streams-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass());

        // Build the topology
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> rawStream = builder.stream(INPUT_TOPIC);

        KStream<String, String> predictions = rawStream.mapValues(value -> {
            try {
                Gson gson = new Gson();
                JsonObject record = gson.fromJson(value, JsonObject.class);

                // Extract features needed by the model
                JsonObject payload = new JsonObject();
                payload.addProperty("season",
                        record.get("season").getAsDouble());
                payload.addProperty("hr",
                        record.get("hr").getAsDouble());
                payload.addProperty("holiday",
                        record.get("holiday").getAsDouble());
                payload.addProperty("weekday",
                        record.get("weekday").getAsDouble());
                payload.addProperty("workingday",
                        record.get("workingday").getAsDouble());
                payload.addProperty("weathersit",
                        record.get("weathersit").getAsDouble());
                payload.addProperty("temp",
                        record.get("temp").getAsDouble());
                payload.addProperty("atemp",
                        record.get("atemp").getAsDouble());
                payload.addProperty("hum",
                        record.get("hum").getAsDouble());
                payload.addProperty("windspeed",
                        record.get("windspeed").getAsDouble());

                // Call Python model server
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(MODEL_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers
                                .ofString(payload.toString()))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                System.out.println("MODEL RESPONSE: " + response.body());
                // Build output message
                JsonObject result = new JsonObject();
                result.add("input", record);
                result.add("prediction",
                        gson.fromJson(response.body(), JsonObject.class));
                return result.toString();

            } catch (Exception e) {
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        });

        // Send predictions to output topic
        predictions.to(OUTPUT_TOPIC);

        // Start the streams app
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        System.out.println("Streams Processor running...");
        System.out.println("Reading from '" + INPUT_TOPIC +
                "' → predicting → writing to '" + OUTPUT_TOPIC + "'");

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(
                new Thread(streams::close)
        );
    }
}