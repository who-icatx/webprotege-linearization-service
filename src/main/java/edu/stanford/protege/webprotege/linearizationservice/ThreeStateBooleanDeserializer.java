package edu.stanford.protege.webprotege.linearizationservice;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import edu.stanford.protege.webprotege.linearizationservice.model.ThreeStateBoolean;

import java.io.IOException;

public class ThreeStateBooleanDeserializer extends JsonDeserializer<ThreeStateBoolean> {

    @Override
    public ThreeStateBoolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null) {
            return ThreeStateBoolean.UNKNOWN;
        }
        return switch (value.toLowerCase()) {
            case "true" -> ThreeStateBoolean.TRUE;
            case "false" -> ThreeStateBoolean.FALSE;
            default -> ThreeStateBoolean.UNKNOWN;
        };
    }
}
