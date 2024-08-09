package edu.stanford.protege.webprotege.linearizationservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.linearizationservice.events.EventProcesableParameter;

public class LinearizationResiduals extends EventProcesableParameter {

    private final ThreeStateBoolean suppressOtherSpecifiedResiduals;

    private final ThreeStateBoolean suppressUnspecifiedResiduals;


    private final String unspecifiedResidualTitle;
    private final String otherSpecifiedResidualTitle;


    @JsonCreator
    public LinearizationResiduals( @JsonProperty("suppressOtherSpecifiedResiduals") ThreeStateBoolean suppressOtherSpecifiedResiduals,
                                   @JsonProperty("suppressUnspecifiedResiduals") ThreeStateBoolean suppressUnspecifiedResiduals,
                                   @JsonProperty("otherSpecifiedResidualTitle") String otherSpecifiedResidualTitle,
                                   @JsonProperty("unspecifiedResidualTitle") String unspecifiedResidualTitle) {
        this.suppressOtherSpecifiedResiduals = suppressOtherSpecifiedResiduals;
        this.unspecifiedResidualTitle = unspecifiedResidualTitle;
        this.suppressUnspecifiedResiduals = suppressUnspecifiedResiduals;
        this.otherSpecifiedResidualTitle = otherSpecifiedResidualTitle;
    }


    @JsonProperty("suppressOtherSpecifiedResiduals")
    public ThreeStateBoolean getSuppressOtherSpecifiedResiduals() {
        return suppressOtherSpecifiedResiduals;
    }

    @JsonProperty("unspecifiedResidualTitle")
    public String getUnspecifiedResidualTitle() {
        return unspecifiedResidualTitle;
    }

    @JsonProperty("suppressUnspecifiedResiduals")
    public ThreeStateBoolean getSuppressUnspecifiedResiduals() {
        return suppressUnspecifiedResiduals;
    }

    @JsonProperty("otherSpecifiedResidualTitle")
    public String getOtherSpecifiedResidualTitle() {
        return otherSpecifiedResidualTitle;
    }
}