package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.EventProcesableParameter;

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

    @JsonProperty("suppressUnspecifiedResiduals")
    public String getUnspecifiedResidualTitle() {
        return unspecifiedResidualTitle;
    }

    @JsonProperty("otherSpecifiedResidualTitle")
    public ThreeStateBoolean getSuppressUnspecifiedResiduals() {
        return suppressUnspecifiedResiduals;
    }

    @JsonProperty("unspecifiedResidualTitle")
    public String getOtherSpecifiedResidualTitle() {
        return otherSpecifiedResidualTitle;
    }
}
