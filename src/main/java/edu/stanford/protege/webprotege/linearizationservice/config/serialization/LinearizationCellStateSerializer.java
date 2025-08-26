package edu.stanford.protege.webprotege.linearizationservice.config.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationStateCell;

import java.io.IOException;

public class LinearizationCellStateSerializer extends StdSerializer<LinearizationStateCell> {

    public LinearizationCellStateSerializer() {
        this(null);
    }

    public LinearizationCellStateSerializer(Class<LinearizationStateCell> t) {
        super(t);
    }

    @Override
    public void serialize(LinearizationStateCell value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.name().toLowerCase());
    }
}
