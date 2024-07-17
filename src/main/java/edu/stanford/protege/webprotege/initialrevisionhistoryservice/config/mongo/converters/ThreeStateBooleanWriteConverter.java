package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.converters;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

public class ThreeStateBooleanWriteConverter implements Converter<ThreeStateBoolean, String> {

    @Override
    public String convert(@NotNull ThreeStateBoolean source) {
        if (source == null) {
            return "unknown";
        }
        return source.name().toLowerCase();
    }
}

