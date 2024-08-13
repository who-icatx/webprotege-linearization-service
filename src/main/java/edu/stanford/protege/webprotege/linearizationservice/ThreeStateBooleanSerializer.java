package edu.stanford.protege.webprotege.linearizationservice;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.stanford.protege.webprotege.linearizationservice.model.ThreeStateBoolean;

import java.io.IOException;

public class ThreeStateBooleanSerializer extends StdSerializer<ThreeStateBoolean> {

    public ThreeStateBooleanSerializer() {
        this(null);
    }

    public ThreeStateBooleanSerializer(Class<ThreeStateBoolean> t) {
        super(t);
    }

    @Override
    public void serialize(ThreeStateBoolean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.name().toLowerCase());
    }
}
