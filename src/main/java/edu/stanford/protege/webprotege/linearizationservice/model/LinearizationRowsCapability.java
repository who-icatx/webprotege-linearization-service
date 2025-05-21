package edu.stanford.protege.webprotege.linearizationservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.authorization.Capability;
import edu.stanford.protege.webprotege.authorization.GenericParameterizedCapability;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;

import java.util.List;
import java.util.Map;

@JsonTypeName(LinearizationRowsCapability.TYPE)
public record LinearizationRowsCapability(@JsonProperty("id") String id,
                                          @JsonProperty("linearizationIds") List<String> linearizationIds,
                                          @JsonProperty("contextCriteria") CompositeRootCriteria contextCriteria) implements Capability {

    public final static String TYPE = "LinearizationRowsCapability";

    public static final String VIEW_LINEARIZATION_ROW = "ViewLinearizationRow";
    public static final String EDIT_LINEARIZATION_ROW = "EditLinearizationRow";

    public static final String VIEW_POSTCOORDINATION_LINEARIZATION_ROW = "ViewPostcoordinationLinearizationRow";

    public static final String EDIT_POSTCOORDINATION_LINEARIZATION_ROW = "EditPostcoordinationLinearizationRow";



    @Override
    public GenericParameterizedCapability asGenericCapability() {
        return new GenericParameterizedCapability(TYPE, id, Map.of("linearizationIds", linearizationIds, "contextCriteria", contextCriteria));
    }
}