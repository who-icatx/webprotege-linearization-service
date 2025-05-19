package edu.stanford.protege.webprotege.linearizationservice.config.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinitionAccessibility;

import java.io.IOException;

public class LinearizationDefinitionAccessibilitySerializer
        extends StdSerializer<LinearizationDefinitionAccessibility> {

    public LinearizationDefinitionAccessibilitySerializer() {
        super(LinearizationDefinitionAccessibility.class);
    }

    @Override
    public void serialize(LinearizationDefinitionAccessibility value,
                          JsonGenerator gen,
                          SerializerProvider provider) throws IOException {
        gen.writeString(value.name());
    }
}
