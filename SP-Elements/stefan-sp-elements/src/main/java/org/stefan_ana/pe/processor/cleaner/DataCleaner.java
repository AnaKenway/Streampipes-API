package org.stefan_ana.pe.processor.cleaner;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.DataProcessorType;
import org.apache.streampipes.model.graph.DataProcessorDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.sdk.builder.ProcessingElementBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.helpers.EpRequirements;
import org.apache.streampipes.sdk.helpers.Labels;
import org.apache.streampipes.sdk.helpers.Locales;
import org.apache.streampipes.sdk.helpers.OutputStrategies;
import org.apache.streampipes.sdk.utils.Assets;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.standalone.ProcessorParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataCleaner extends StreamPipesDataProcessor {
    private static final String ID = "org.stefan_ana.pe.processor.cleaner.DataCleaner";

    private static final String TIMESTAMPS_PROPERTY_KEY = "timestamps-property-key";
    private static final String FILL_VALUES_KEY = "fill-values-key";
    private static final String GROUP_KEY = "group-key";
    private static final String PROPERTY_KEY = "property-key";


    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder
                .create(ID)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .withLocales(Locales.EN)
                .category(DataProcessorType.AGGREGATE)
                .requiredStream(StreamRequirementsBuilder
                        .create()
                        .requiredPropertyWithNaryMapping(
                                EpRequirements.timestampReq(),
                                Labels.withId(TIMESTAMPS_PROPERTY_KEY),
                                PropertyScope.NONE)
                        .build())
                .outputStrategy(OutputStrategies.keep())
                .build();
    }

    @Override
    public void onInvocation(ProcessorParams processorParams, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext eventProcessorRuntimeContext) throws SpRuntimeException {
    }

    @Override
    public void onEvent(Event event, SpOutputCollector spOutputCollector) throws SpRuntimeException {

        Map<String, Object> properties = event.getRaw();
        String key = "data_array";
        ArrayList<HashMap<String,Object>> property = (ArrayList<HashMap<String, Object>>) properties.get(key);

        int windowSizeRight = 2;
        int windowSizeLeft = 2;
        Object obj = null;

        try {
            sendToSPAPI(properties.toString(), "http://localhost:8080/monitoringdata");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < property.size(); i++) {
            obj = property.get(i);
            if (((HashMap<String, Object>) obj).get("temperature") == null) {
                property = fillMissingValue(property, i, windowSizeLeft, windowSizeRight);
                windowSizeLeft++;
                if (windowSizeRight > 1)
                    windowSizeRight--;
            }
        }

        for (int i = 0; i < event.getFields().keySet().size(); i++)
            event.removeFieldBySelector(event.getFields().keySet().toArray()[i].toString());

        properties.keySet().forEach(key1 -> {
            event.addField("s0::" + key1, properties.get(key));
        });

        properties.replace("isClean", true);

        try {
            sendToSPAPI(properties.toString(), "http://localhost:8080/monitoringdata");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        spOutputCollector.collect(event);
    }

    private ArrayList<HashMap<String, Object>> fillMissingValue(ArrayList<HashMap<String, Object>> property, int i, int windowSizeLeft, int windowSizeRight) {
        Double value = null;
        int index = i;
        ArrayList<Number> valuesForCalculating = new ArrayList<Number>();
        int sizeLeft = windowSizeLeft;
        int sizeRight = windowSizeRight;

        for (int j = 1; j <= sizeLeft; j++) // add windowSize values from left side of value
            if (i - j > -1) {
                if ((Number) property.get(i - j).get("temperature") == null)
                    continue;
                valuesForCalculating.add((Number) property.get(i - j).get("temperature"));
            }

        for (int j = 1; j <= sizeRight; j++) { // add windowSize values from right side of value

            if (i + j >= property.size())
                break;

            if (property.get(i + j).get("temperature") != null)
                valuesForCalculating.add((Number)property.get(i + j).get("temperature"));
            else {
                j--;
                i++;
            }
        }

        value = 0.0;
        for (int j = 0; j < valuesForCalculating.size(); j++)
            value += valuesForCalculating.get(j).doubleValue();
        Double result = value / valuesForCalculating.size();
        HashMap<String, Object> temp = property.get(i);
        temp.remove("temperature");
        temp.put("temperature", result);
        property.set(index, temp);
        return property;
    }
    private void sendToSPAPI(String data, String uri) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost http = new HttpPost(uri);
        http.setHeader("Content-Type", "application/json");

        http.setEntity(new StringEntity(data
                .replace("isClean","\"isClean\"")
                .replace("data_array","\"data_array\"")
                .replace("timestamp","\"timestamp\"")
                .replace("temperature", "\"temperature\"")
                .replace("pressure","\"pressure\"")
                .replace("=",":")));

        CloseableHttpResponse response = client.execute(http);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity);

        client.close();
    }

    @Override
    public void onDetach() throws SpRuntimeException {
    }
}
