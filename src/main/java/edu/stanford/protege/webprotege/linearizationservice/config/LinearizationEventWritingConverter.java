package edu.stanford.protege.webprotege.linearizationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.linearizationservice.events.LinearizationEvent;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;


@WritingConverter
public class LinearizationEventWritingConverter implements Converter<LinearizationEvent, Document> {
    private final ObjectMapper objectMapper;

    public LinearizationEventWritingConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public Document convert(LinearizationEvent source) {
        return objectMapper.convertValue(source, Document.class);
    }
}