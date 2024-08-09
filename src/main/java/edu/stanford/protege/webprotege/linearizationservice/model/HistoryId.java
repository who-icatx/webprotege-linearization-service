package edu.stanford.protege.webprotege.linearizationservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoryId {

    private final String id;


    @JsonCreator
    public HistoryId(@JsonProperty("$oid") String id) {
        this.id = id;
    }
}
