package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.converters;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

public class ThreeStateBooleanReadConverter implements Converter<String, ThreeStateBoolean> {

    @Override
    public ThreeStateBoolean convert(@NotNull String source) {
        if (source == null) {
            return ThreeStateBoolean.UNKNOWN;
        }
        return switch (source.toLowerCase()) {
            case "true" -> ThreeStateBoolean.TRUE;
            case "false" -> ThreeStateBoolean.FALSE;
            default -> ThreeStateBoolean.UNKNOWN;
        };
    }
}




