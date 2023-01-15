package org.stefan_ana.pe.processor.CEP;

import com.github.underscore.lodash.Json;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.output.StreamCallback;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.container.api.ResolvesContainerProvidedOutputStrategy;
import org.apache.streampipes.model.DataProcessorType;
import org.apache.streampipes.model.graph.DataProcessorDescription;
import org.apache.streampipes.model.graph.DataProcessorInvocation;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.model.schema.EventProperty;
import org.apache.streampipes.model.schema.EventSchema;
import org.apache.streampipes.model.schema.PropertyScope;
import org.apache.streampipes.sdk.builder.ProcessingElementBuilder;
import org.apache.streampipes.sdk.builder.StreamRequirementsBuilder;
import org.apache.streampipes.sdk.extractor.ProcessingElementParameterExtractor;
import org.apache.streampipes.sdk.helpers.*;
import org.apache.streampipes.sdk.utils.Assets;
import org.apache.streampipes.sdk.utils.Datatypes;
import org.apache.streampipes.vocabulary.SO;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.standalone.ProcessorParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataProcessor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ComplexEventProcessing extends StreamPipesDataProcessor
        implements ResolvesContainerProvidedOutputStrategy<DataProcessorInvocation, ProcessingElementParameterExtractor> {
    public static final String ID = "org.stefan_ana.pe.processor.CEP.ComplexEventProcessing";
    private SiddhiManager siddhiManager;
    private SiddhiAppRuntime siddhiAppRuntime1;
    private SiddhiAppRuntime siddhiAppRuntime2;
    private InputHandler inputHandler1;
    private InputHandler inputHandler2;
    private static final String PROPERTY_KEY = "property-key";
    private static final String WINDOW_LENGTH_KEY = "window-length-key";
    private static final String WINDOW_LENGTH_KEY_CRITICAL = "window-length-key-critical";
    private static final String OPERATOR_KEY = "operator-key";
    private static final String COMPARISON_VALUE_KEY = "comparison-value-key2";
    private static final String OUTPUT_NAME_KEY = "output-name-key";
    private String propertyName;
    private Integer windowLength;
    private Integer windowLengthCritical;
    private String operator;
    private Integer comparisonValue;
    private String outputPropertyName;

    @Override
    public DataProcessorDescription declareModel() {
        return ProcessingElementBuilder
                .create(ID)
                .withAssets(Assets.DOCUMENTATION, Assets.ICON)
                .withLocales(Locales.EN)
                .category(DataProcessorType.AGGREGATE)
                .requiredStream(StreamRequirementsBuilder
                        .create()
                        .requiredPropertyWithUnaryMapping(
                                EpRequirements.numberReq(),
                                Labels.withId(PROPERTY_KEY),
                                PropertyScope.NONE)
                        .build())
                .requiredIntegerParameter(Labels.withId(WINDOW_LENGTH_KEY), 24)
                .requiredIntegerParameter(Labels.withId(WINDOW_LENGTH_KEY_CRITICAL), 6)
                .requiredSingleValueSelection(
                        Labels.withId(OPERATOR_KEY),
                        Options.from("<", "<=", ">", ">=", "==", "!="))
                .requiredIntegerParameter((Labels.withId(COMPARISON_VALUE_KEY)), 1, 600, 1)
                .requiredTextParameter(Labels.withId(OUTPUT_NAME_KEY), false, false)
                .outputStrategy(OutputStrategies.customTransformation())
                .build();
    }

    @Override
    public void onInvocation(ProcessorParams parameters, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext runtimeContext) throws SpRuntimeException {
        propertyName = parameters.extractor().mappingPropertyValue(PROPERTY_KEY);
        windowLength = parameters.extractor().singleValueParameter(WINDOW_LENGTH_KEY, Integer.class);
        operator = parameters.extractor().selectedSingleValue(OPERATOR_KEY, String.class);
        comparisonValue = parameters.extractor().singleValueParameter(COMPARISON_VALUE_KEY, Integer.class);
        outputPropertyName = parameters.extractor().singleValueParameter(OUTPUT_NAME_KEY, String.class);
        windowLengthCritical = parameters.extractor().singleValueParameter(WINDOW_LENGTH_KEY_CRITICAL, Integer.class);

        siddhiManager = new SiddhiManager();
        siddhiAppRuntime1 = siddhiManager.createSiddhiAppRuntime(createQuery1());
        siddhiAppRuntime2 = siddhiManager.createSiddhiAppRuntime(createQuery2());

        siddhiAppRuntime1.addCallback("WarningAlarmStream", new StreamCallback() {
            @Override
            public void receive(io.siddhi.core.event.Event[] events) {
                if ((Boolean) events[0].getData(0)) {
                    try {
                        inputHandler2.send(new Object[]{events[0].getData(2), events[0].getData(1)});
                        try {
                            JSONObject warning = new JSONObject();
                            warning.put("warning", (Boolean) events[0].getData(0)? 1 : 0);
                            warning.put("timestamp", events[0].getData(2) );
                            warning.put("value", events[0].getData(1));

                            sendToSPAPI(warning.toString(), "http://localhost:8080/warning");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Event eventNew = new Event();
                        eventNew.addField("timestamp", System.currentTimeMillis());
                        eventNew.addField(outputPropertyName, true);
                        eventNew.addField("WarningData", events[0].getData(1));
                        eventNew.addField("CriticalData", "No critical warning.");
                        spOutputCollector.collect(eventNew);
                        logger.info("Siddhi: Data sent to second stream: (" + Date.from(Instant.now()) + ")");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        siddhiAppRuntime2.addCallback("CriticalAlarmStream", new StreamCallback() {
            @Override
            public void receive(io.siddhi.core.event.Event[] events) {
                if ((Boolean) events[0].getData(0)) {
                    try {
                        JSONObject criticalAlarm = new JSONObject();
                        criticalAlarm.put("critical_alarm", (Boolean) events[0].getData(0)? 1 : 0);
                        criticalAlarm.put("timestamp", events[0].getData(1) );
                        criticalAlarm.put("value", events[0].getData(2));

                        sendToSPAPI(criticalAlarm.toString(), "http://localhost:8080/criticalalarm");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Event eventNew = new Event();
                    eventNew.addField("timestamp", System.currentTimeMillis());
                    eventNew.addField(outputPropertyName, true);
                    eventNew.addField("WarningData", "No warning data, it is critical warning.");
                    eventNew.addField("CriticalData", events[0].getData(1));
                    spOutputCollector.collect(eventNew);
                    logger.info("Siddhi: Critical alarm sent to next pipeline element: (" + Date.from(Instant.now()) + ")");
                }
            }
        });
        inputHandler1 = siddhiAppRuntime1.getInputHandler("WarningStream");
        inputHandler2 = siddhiAppRuntime2.getInputHandler("CriticalStream");
        siddhiAppRuntime1.start();
        siddhiAppRuntime2.start();
    }

    private String createQuery1() {
        String defineStream = "define stream WarningStream(value double, time long); ";

        String from = "from " +
                "WarningStream" + "#window.length(" + windowLength + ")\n";
        String select = "select ifThenElse(value" + operator + comparisonValue + ", true, false) as warning," +
                "value as average, time as timestamp\n";
        String insertInto = "insert into WarningAlarmStream;";
        return defineStream + from + select + insertInto;
    }

    private String createQuery2() {
        String defineStream = "define stream CriticalStream(time long, val double); " +
                "@info(name = 'queryCriticalWarning') \n";

        int threshold = 5;//(windowLengthCritical / periodMinutes) / 2;
        String from = "from " +
                "CriticalStream" + "#window.time(" + windowLengthCritical + " min)\n";
        String select = "select ifThenElse(count()>" + threshold + ", true, false) as criticalAlarm," +
                " time as timestamp,\n" +
                " val as value\n";
        String insertInto = "insert into CriticalAlarmStream;";
        return defineStream + from + select + insertInto;
    }

    @Override
    public void onEvent(Event event, SpOutputCollector collector) throws SpRuntimeException {
        ArrayList<HashMap> res = (ArrayList<HashMap>) event.getRaw().get("s0::data_array");
        for(int i =0; i< res.size(); i++) {
            try {
                inputHandler1.send(new Object[]{res.get(i).get("temperature"), res.get(i).get("timestamp")});
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        logger.info("Siddhi: Data sent to first stream: (" + Date.from(Instant.now()) + ")");
    }

    @Override
    public void onDetach() throws SpRuntimeException {
        siddhiAppRuntime1.shutdown();
        siddhiAppRuntime2.shutdown();
        siddhiManager.shutdown();
    }

    @Override
    public EventSchema resolveOutputStrategy(DataProcessorInvocation processingElement, ProcessingElementParameterExtractor parameterExtractor) throws SpRuntimeException {
        if (parameterExtractor.singleValueParameter(OUTPUT_NAME_KEY, String.class) == null)
            return null;

        outputPropertyName =
                parameterExtractor.singleValueParameter(OUTPUT_NAME_KEY, String.class);

        List<EventProperty> eventProperties = new ArrayList<>();
        eventProperties.add(
                EpProperties.booleanEp(
                        new Label(
                                outputPropertyName,
                                "Trend",
                                "True/False depending whether trend was detected"),
                        outputPropertyName,
                        SO.Boolean));
        eventProperties.add(EpProperties.listEp(
                new Label("WarningData",
                        "WarningData", ""),
                "WarningData",
                Datatypes.String,
                SO.Number));
        eventProperties.add(EpProperties.listEp(
                new Label("CriticalData",
                        "CriticalData", ""),
                "CriticalData",
                Datatypes.String,
                SO.Number));
        eventProperties.add(EpProperties.numberEp(
                new Label("timestamp", "timestamp", ""),
                "timestamp",
                SO.DateTime));

        return new EventSchema(eventProperties);
    }

    private void sendToSPAPI(String data, String uri) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost http = new HttpPost(uri);
        http.setHeader("Content-Type", "application/json");

        http.setEntity(new StringEntity(data));

        CloseableHttpResponse response = client.execute(http);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity);

        client.close();
    }
}
