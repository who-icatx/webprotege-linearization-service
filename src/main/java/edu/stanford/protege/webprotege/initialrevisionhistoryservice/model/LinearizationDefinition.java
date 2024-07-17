package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LinearizationDefinition {


    @JsonProperty("Id")
    private final String id;

    @JsonProperty("whoficEntityIri")
    private final String whoficEntityIri;

    @JsonProperty("linearizationMode")
    private final String linearizationMode;

    @JsonProperty("rootId")
    private final String rootId;


    @JsonProperty("coreLinId")
    private final String coreLinId;

    @JsonProperty("sortingCode")
    private final String sortingCode;


    @JsonCreator
    public LinearizationDefinition(@JsonProperty("Id") String id,
                                   @JsonProperty("whoficEntityIri") String whoficEntityIri,
                                   @JsonProperty("linearizationMode") String linearizationMode,
                                   @JsonProperty("rootId") String rootId,
                                   @JsonProperty("coreLinId") String coreLinId,
                                   @JsonProperty("sortingCode") String sortingCode) {
        this.id = id;
        this.whoficEntityIri = whoficEntityIri;
        this.linearizationMode = linearizationMode;
        this.rootId = rootId;
        this.coreLinId = coreLinId;
        this.sortingCode = sortingCode;
    }
}
