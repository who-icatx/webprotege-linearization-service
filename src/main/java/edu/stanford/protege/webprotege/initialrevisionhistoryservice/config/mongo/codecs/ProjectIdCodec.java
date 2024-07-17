package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import edu.stanford.protege.webprotege.common.ProjectId;

public class ProjectIdCodec implements Codec<ProjectId> {

    @Override
    public ProjectId decode(BsonReader reader, DecoderContext decoderContext) {
        return ProjectId.valueOf(reader.readString());
    }

    @Override
    public void encode(BsonWriter writer, ProjectId value, EncoderContext encoderContext) {
        writer.writeString(value.value());
    }

    @Override
    public Class<ProjectId> getEncoderClass() {
        return ProjectId.class;
    }
}

