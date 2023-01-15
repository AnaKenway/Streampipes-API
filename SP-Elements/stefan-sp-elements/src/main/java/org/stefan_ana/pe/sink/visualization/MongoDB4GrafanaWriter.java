package org.stefan_ana.pe.sink.visualization;

import org.apache.streampipes.commons.exceptions.SpRuntimeException;
import org.apache.streampipes.model.graph.DataSinkDescription;
import org.apache.streampipes.model.runtime.Event;
import org.apache.streampipes.wrapper.context.EventSinkRuntimeContext;
import org.apache.streampipes.wrapper.standalone.SinkParams;
import org.apache.streampipes.wrapper.standalone.StreamPipesDataSink;

public class MongoDB4GrafanaWriter extends StreamPipesDataSink {
    @Override
    public DataSinkDescription declareModel() {
        return null;
    }

    @Override
    public void onInvocation(SinkParams parameters, EventSinkRuntimeContext runtimeContext) throws SpRuntimeException {

    }

    @Override
    public void onEvent(Event event) throws SpRuntimeException {

    }

    @Override
    public void onDetach() throws SpRuntimeException {

    }
}
