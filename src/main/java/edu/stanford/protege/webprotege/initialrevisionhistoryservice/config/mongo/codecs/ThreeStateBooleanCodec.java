package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;

public class ThreeStateBooleanCodec implements Codec<ThreeStateBoolean> {

    @Override
    public ThreeStateBoolean decode(BsonReader reader, DecoderContext decoderContext) {
        String value = reader.readString();
        return ThreeStateBoolean.valueOf(value.toUpperCase());
    }

    @Override
    public void encode(BsonWriter writer, ThreeStateBoolean value, EncoderContext encoderContext) {
        writer.writeString(value.name());
    }

    @Override
    public Class<ThreeStateBoolean> getEncoderClass() {
        return ThreeStateBoolean.class;
    }
}
