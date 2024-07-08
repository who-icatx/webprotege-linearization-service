package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationResponse;

public class LinearizationResiduals extends LinearizationResponse {

    private final @JsonProperty("suppressOtherSpecifiedResiduals") ThreeStateBoolean suppressSpecifiedResidual;
    private final @JsonProperty("unspecifiedResidualTitle") String unspecifiedResidualTitle;

    public LinearizationResiduals(ThreeStateBoolean suppressSpecifiedResidual, String unspecifiedResidualTitle) {
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
