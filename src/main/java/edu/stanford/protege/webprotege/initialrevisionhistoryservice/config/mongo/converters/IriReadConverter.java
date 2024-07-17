package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.converters;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.semanticweb.owlapi.model.IRI;

public class IriReadConverter implements Converter<String, IRI> {
    @Override
    public IRI convert(@NotNull String source) {
        return IRI.create(source);
    }
}
