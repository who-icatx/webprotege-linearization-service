package edu.stanford.protege.webprotege.linearizationservice.config.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationStateCell;

import java.io.IOException;

public class ThreeStateBooleanDeserializer extends JsonDeserializer<LinearizationStateCell> {

    @Override
    public LinearizationStateCell deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null) {
            return LinearizationStateCell.UNKNOWN;
        }
        return switch (value.toLowerCase()) {
            case "true" -> LinearizationStateCell.TRUE;
            case "false" -> LinearizationStateCell.FALSE;
            case "follow_base_linearization" -> LinearizationStateCell.FOLLOW_BASE_LINEARIZATION;
            default -> LinearizationStateCell.UNKNOWN;
        };
    }
}
