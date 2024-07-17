package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EntityLinearizationHistoryMapper {
    private final ObjectMapper objectMapper;

    public EntityLinearizationHistoryMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
