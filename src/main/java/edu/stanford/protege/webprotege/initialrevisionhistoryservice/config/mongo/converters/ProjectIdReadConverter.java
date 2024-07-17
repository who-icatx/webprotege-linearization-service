package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.mongo.converters;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import edu.stanford.protege.webprotege.common.ProjectId;

public class ProjectIdReadConverter implements Converter<String, ProjectId> {
    @Override
    public ProjectId convert(@NotNull String source) {
        return ProjectId.valueOf(source);
    }
}

