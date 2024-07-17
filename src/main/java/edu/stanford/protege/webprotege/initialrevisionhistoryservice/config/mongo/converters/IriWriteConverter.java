package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.converters;

import org.springframework.core.convert.converter.Converter;
import org.semanticweb.owlapi.model.IRI;

public class IriWriteConverter implements Converter<IRI, String> {
    @Override
    public String convert(IRI source) {
        return source.toString();
    }
}

