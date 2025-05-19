package edu.stanford.protege.webprotege.linearizationservice.model;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.authorization.*;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;

import java.util.*;

@JsonTypeName(LinearizationResidualsCapability.TYPE)
public record LinearizationResidualsCapability(@JsonProperty("id") String id,
                                               @JsonProperty("contextCriteria") CompositeRootCriteria contextCriteria) implements Capability {

    public final static String TYPE = "LinearizationResidualsCapability";

    public static final String VIEW_LINEARIZATION_RESIDUALS = "ViewLinearizationResiduals";
    public static final String EDIT_LINEARIZATION_RESIDUALS = "EditLinearizationResiduals";

    @Override
    public GenericParameterizedCapability asGenericCapability() {
        return new GenericParameterizedCapability(TYPE, id, Map.of( "contextCriteria", contextCriteria));
    }
}