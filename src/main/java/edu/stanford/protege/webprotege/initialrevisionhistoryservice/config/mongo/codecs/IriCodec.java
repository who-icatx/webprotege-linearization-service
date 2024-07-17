package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.semanticweb.owlapi.model.IRI;

public class IriCodec implements Codec<IRI> {

    @Override
    public IRI decode(BsonReader reader, DecoderContext decoderContext) {
        return IRI.create(reader.readString());
    }

    @Override
    public void encode(BsonWriter writer, IRI value, EncoderContext encoderContext) {
        writer.writeString(value.toString());
    }

    @Override
    public Class<IRI> getEncoderClass() {
        return IRI.class;
    }
}

