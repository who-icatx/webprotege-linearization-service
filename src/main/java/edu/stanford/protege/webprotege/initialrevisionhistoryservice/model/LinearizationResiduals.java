package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.EventProcesableParameter;

public class LinearizationResiduals extends EventProcesableParameter {

    private final ThreeStateBoolean suppressSpecifiedResidual;
    private final String unspecifiedResidualTitle;


    @JsonCreator
    public LinearizationResiduals( @JsonProperty("suppressOtherSpecifiedResiduals") ThreeStateBoolean suppressSpecifiedResidual,
                                   @JsonProperty("unspecifiedResidualTitle") String unspecifiedResidualTitle) {
        this.suppressSpecifiedResidual = suppressSpecifiedResidual;
        this.unspecifiedResidualTitle = unspecifiedResidualTitle;
    }


    public ThreeStateBoolean getSuppressSpecifiedResidual() {
        return suppressSpecifiedResidual;
    }

    public String getUnspecifiedResidualTitle() {
        return unspecifiedResidualTitle;
    }
}
