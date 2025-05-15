package edu.stanford.protege.webprotege.linearizationservice.config.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinitionAccessibility;

import java.io.IOException;

public class LinearizationDefinitionAccessibilityDeserializer
        extends StdDeserializer<LinearizationDefinitionAccessibility> {

    public LinearizationDefinitionAccessibilityDeserializer() {
        super(LinearizationDefinitionAccessibility.class);
    }

    @Override
    public LinearizationDefinitionAccessibility deserialize(JsonParser p,
                                                            DeserializationContext ctxt)
                                                            throws IOException {
        String text = p.getText();
        return LinearizationDefinitionAccessibility.valueOf(text.toUpperCase());
    }
}
