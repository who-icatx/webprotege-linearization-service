package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.converters;

import org.springframework.core.convert.converter.Converter;
import edu.stanford.protege.webprotege.common.ProjectId;

public class ProjectIdWriteConverter implements Converter<ProjectId, String> {
    @Override
    public String convert(ProjectId source) {
        return source.value();
    }
}

