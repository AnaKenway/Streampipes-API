package org.stefan_ana.pe.source.data.collector;

import org.apache.streampipes.messaging.kafka.SpKafkaProducer;
import org.apache.streampipes.model.SpDataStream;
import org.apache.streampipes.sdk.builder.DataStreamBuilder;
import org.apache.streampipes.sdk.helpers.*;
import org.apache.streampipes.sdk.utils.Assets;
import org.apache.streampipes.sources.AbstractAdapterIncludedStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DataCollector extends AbstractAdapterIncludedStream {

    private static final String ID = "org.stefan_ana.pe.source.data.collector.DataCollector";

    private static final String KAFKA_TOPIC = "org.stefan_ana.pe.source.stream.data.reader";
    private static final String KAFKA_HOST = System.getenv("KAFKA_HOST");
    private static final String KAFKA_PORT = System.getenv("KAFKA_PORT");

    @Override
    public SpDataStream declareModel() {
        return DataStreamBuilder
                .create(ID)
                .withLocales(Locales.EN)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .property(EpProperties.timestampProperty("timestamp"))
                .property(EpProperties.stringEp(Labels.from("data_array", "data_array", "data_array"), "data_array", "http://stefan_ana/data_array"))
                .format(Formats.jsonFormat())
                .protocol(Protocols.kafka(KAFKA_HOST, Integer.parseInt(KAFKA_PORT), KAFKA_TOPIC))
                .build();
    }

    @Override
    public void executeStream() {

        Runnable run = () -> {
            int i = 0;
            SpKafkaProducer spKafkaProducer = new SpKafkaProducer(KAFKA_HOST + ":" + KAFKA_PORT, KAFKA_TOPIC, Collections.emptyList());

            // Set the minimum and maximum  values
            double minTemp = 25;
            double maxTemp = 35;
            double minPressure = 1000;
            double maxPressure = 2000;
            // Set the length of time to generate data for (number * 2 seconds)
            int timeLength = 30;

            // Initialize the list to hold the data
            JSONArray data = new JSONArray();

            while (true) {

                // Set the starting time
                long startTime = System.currentTimeMillis();

                // Calculate the current time
                long currTime = startTime - timeLength * 2000 + (i * 2000);

                // Generate a random values
                double temperature = minTemp + (Math.random() * (maxTemp - minTemp));
                double pressure = minPressure + (Math.random() * (maxPressure - minPressure));
                // Occasionally generate an outlier
                if (Math.random() < 0.1) {
                    temperature = maxTemp + 10 + (Math.random() * 10);
                    pressure = maxPressure + 100 + (Math.random() * 100);
                }

                // Add the data point to the list
                JSONObject measurement = new JSONObject();
                measurement.put("temperature", temperature);
                measurement.put("timestamp", currTime);
                measurement.put("pressure", pressure);
                data.put(measurement);

                i++;
                if (i == timeLength) {
                    JSONObject obj = new JSONObject();
                    obj.put("data_array", possibleNullValues(data));
                    obj.put("timestamp", startTime);
                    obj.put("isClean", false);

                    spKafkaProducer.publish(obj.toString());
                    data = new JSONArray();
                    i = 0;
                    try {
                        TimeUnit.SECONDS.sleep(60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(run).start();
    }

    private JSONArray possibleNullValues(JSONArray data) {
        if (Math.random() < 0.95) {
            for (int i = 5; i < 10; i++) {
                JSONObject obj = (JSONObject) data.get(i);
                obj.remove("temperature");
                obj.put("temperature", JSONObject.NULL);
                data.put(i, obj);
            }
            return data;
        }
        return data;
    }
}