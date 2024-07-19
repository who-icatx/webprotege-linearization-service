package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;


public abstract class LinearizationSpecificationEvent implements LinearizationEvent {

    private final String linearizationView;


    protected LinearizationSpecificationEvent(String linearizationView) {
        this.linearizationView = linearizationView;
    }


    @JsonProperty("linearizationView")
    public String getLinearizationView() {
        return linearizationView;
    }

}
