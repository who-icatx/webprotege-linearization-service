package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

public class SetAuxiliaryAxisChild extends LinearizationSpecificationEvent {

    private final ThreeStateBoolean value;

    public final static String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetAuxiliaryAxisChild";

    @JsonCreator
    public SetAuxiliaryAxisChild(@JsonProperty("value") ThreeStateBoolean value, @JsonProperty("linearizationView") String linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {

        if (!(event instanceof LinearizationSpecification specification)) {
            throw new RuntimeException("Error! Trying to parse event" + LinearizationSpecification.class.getName());
        }

        if (notEqual(specification.getIsAuxiliaryAxisChild(), value)) {
            return new LinearizationSpecification(value,
                    specification.getIsGrouping(),
                    specification.getIsIncludedInLinearization(),
                    specification.getLinearizationParent(),
                    specification.getLinearizationView(),
                    specification.getCodingNote());
        }

        return specification;
    }

    @Override
    public String getType() {
        return SetAuxiliaryAxisChild.class.getName();
    }

    public static String getName() {
        return SetAuxiliaryAxisChild.class.getName();
    }

    public String getValue() {
        return this.value.name();
    }
}
