package org.stefan_ana.pe.processor.warning.checker;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.graph.DataProcessorDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.wrapper.context.EventProcessorRuntimeContext;
import org.apache.streampipes.wrapper.routing.SpOutputCollector;
import org.apache.streampipes.wrapper.standalone.ProcessorParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataProcessor;

public class WarningChecker extends StreamPipesDataProcessor {
    @Override
    public DataProcessorDescription declareModel() {
        return null;
    }

    @Override
    public void onInvocation(ProcessorParams parameters, SpOutputCollector spOutputCollector, EventProcessorRuntimeContext runtimeContext) throws SpRuntimeException {

    }

    @Override
    public void onEvent(Event event, SpOutputCollector collector) throws SpRuntimeException {

    }

    @Override
    public void onDetach() throws SpRuntimeException {

    }
}
