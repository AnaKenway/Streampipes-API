package org.stefan_ana;

import org.apache.streampipes.model.schema.EventProperty;
import org.apache.streampipes.sdk.extractor.AbstractParameterExtractor;
import org.apache.streampipes.sdk.utils.Datatypes;

public class SPHelper {

    public static String getEventPropertyName(String property) {

        String[] parts = property.split("::");

        return parts[parts.length - 1];
    }

    public static String getEventPropertySelector(String stream, String property) {

        return stream.concat("::").concat(property);
    }

    public static Boolean isNumber(AbstractParameterExtractor<?> extractor, EventProperty property) {

        return extractor.comparePropertyRuntimeType(property, Datatypes.Number) ||
                extractor.comparePropertyRuntimeType(property, Datatypes.Integer) ||
                extractor.comparePropertyRuntimeType(property, Datatypes.Float) ||
                extractor.comparePropertyRuntimeType(property, Datatypes.Double) ||
                extractor.comparePropertyRuntimeType(property, Datatypes.Long);
    }
}
