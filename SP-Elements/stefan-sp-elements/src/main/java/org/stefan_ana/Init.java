package org.stefan_ana;

import org.apache.streampipes.container.extensions.ExtensionsModelSubmitter;
import org.apache.streampipes.container.model.SpServiceDefinition;
import org.apache.streampipes.container.model.SpServiceDefinitionBuilder;
import org.apache.streampipes.dataformat.cbor.CborDataFormatFactory;
import org.apache.streampipes.dataformat.fst.FstDataFormatFactory;
import org.apache.streampipes.dataformat.json.JsonDataFormatFactory;
import org.apache.streampipes.dataformat.smile.SmileDataFormatFactory;
import org.apache.streampipes.messaging.jms.SpJmsProtocolFactory;
import org.apache.streampipes.messaging.kafka.SpKafkaProtocolFactory;
import org.apache.streampipes.messaging.mqtt.SpMqttProtocolFactory;
import org.stefan_ana.pe.processor.CEP.ComplexEventProcessing;
import org.stefan_ana.pe.processor.cleaner.DataCleaner;
import org.stefan_ana.config.ConfigKeys;
import org.stefan_ana.pe.source.data.collector.DataCollector;


public class Init extends ExtensionsModelSubmitter {

    public static void main(String[] args) {
        new Init().init();
    }

    @Override
    public SpServiceDefinition provideServiceDefinition() {
        return SpServiceDefinitionBuilder.create("org.stefan_ana",
                "SP elements",
                "StreamPipes elements which are developed by Stefan&Ana", 8090)
                // DATA SOURCES
                .registerPipelineElement(new DataCollector())
                // DATA PROCESSORS
                .registerPipelineElement(new DataCleaner())
                .registerPipelineElement(new ComplexEventProcessing())

                .registerMessagingFormats(
                        new JsonDataFormatFactory(),
                        new CborDataFormatFactory(),
                        new SmileDataFormatFactory(),
                        new FstDataFormatFactory())
                .registerMessagingProtocols(
                        new SpKafkaProtocolFactory(),
                        new SpJmsProtocolFactory(),
                        new SpMqttProtocolFactory())
                .addConfig(ConfigKeys.HOST, System.getenv("SERVICE_HOST"), "Data processor host")
                .addConfig(ConfigKeys.PORT, System.getenv("SERVICE_PORT"), "Data processor port")
                .addConfig(ConfigKeys.SERVICE_NAME, "Pipeline Elements", "Data processor service name")
                .addConfig(ConfigKeys.KAFKA_HOST, System.getenv("KAFKA_HOST"), "Hostname for backend service for kafka")
                .addConfig(ConfigKeys.KAFKA_PORT, System.getenv("KAFKA_PORT"), "Port for backend service for kafka")
                .addConfig(ConfigKeys.CONNECT_CONTAINER_WORKER_HOST, System.getenv("SERVICE_HOST"), "The hostname of the connect container")
                .addConfig(ConfigKeys.CONNECT_CONTAINER_WORKER_PORT, System.getenv("SERVICE_PORT"), "The port of the connect container")
                .addConfig(ConfigKeys.BACKEND_HOST, System.getenv("SP_BACKEND_HOST") == null ? System.getenv("SP_BACKEND_HOST") : "backend", "The host of the backend to register the worker")
                .addConfig(ConfigKeys.BACKEND_PORT, 8030, "The port of the backend to register the worker")
                .build();
    }
}
