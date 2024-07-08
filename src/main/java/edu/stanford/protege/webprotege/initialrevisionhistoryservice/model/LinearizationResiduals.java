package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationResponse;

import javax.sound.sampled.Line;

public class LinearizationResiduals extends LinearizationResponse {

    private @JsonProperty("suppressOtherSpecifiedResiduals") ThreeStateBoolean suppressSpecifiedResidual;
    private @JsonProperty("unspecifiedResidualTitle") String unspecifiedResidualTitle;

    public LinearizationResiduals(ThreeStateBoolean suppressSpecifiedResidual, String unspecifiedResidualTitle) {
        this.suppressSpecifiedResidual = suppressSpecifiedResidual;
        this.unspecifiedResidualTitle = unspecifiedResidualTitle;
    }

    public LinearizationResiduals(){}


    public ThreeStateBoolean getSuppressSpecifiedResidual() {
        return suppressSpecifiedResidual;
    }

    public String getUnspecifiedResidualTitle() {
        return unspecifiedResidualTitle;
    }

    public void setSuppressSpecifiedResidual(ThreeStateBoolean suppressSpecifiedResidual) {
        this.suppressSpecifiedResidual = suppressSpecifiedResidual;
    }

    public void setUnspecifiedResidualTitle(String unspecifiedResidualTitle) {
        this.unspecifiedResidualTitle = unspecifiedResidualTitle;
    }
}
