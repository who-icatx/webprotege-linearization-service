package edu.stanford.protege.webprotege.linearizationservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


public abstract class LinearizationSpecificationEvent implements LinearizationEvent {

    private final String linearizationView;


    protected LinearizationSpecificationEvent(String linearizationView) {
        this.linearizationView = linearizationView;
    }


    @JsonProperty("linearizationView")
    public String getLinearizationView() {
        return linearizationView;
    }

    @Override
    public String toString() {
        return "LinearizationSpecificationEvent{" +
                "linearizationView='" + linearizationView + '\'' +
                "linearizationValue='" + getValue() + '\'' +
                "linearizationType='" + getType() + '\'' +

                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinearizationSpecificationEvent event = (LinearizationSpecificationEvent) o;
        return Objects.equals(linearizationView, event.linearizationView) &&
                Objects.equals(getType(), event.getType()) &&
                Objects.equals(getValue(), event.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(linearizationView);
    }
}
