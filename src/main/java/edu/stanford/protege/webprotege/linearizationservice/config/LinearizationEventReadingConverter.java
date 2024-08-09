package edu.stanford.protege.webprotege.linearizationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.linearizationservice.events.LinearizationEvent;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class LinearizationEventReadingConverter implements Converter<Document, LinearizationEvent> {


    private final ObjectMapper objectMapper;

    public LinearizationEventReadingConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LinearizationEvent convert(Document source) {
        return objectMapper.convertValue(source, LinearizationEvent.class);
    }
}
