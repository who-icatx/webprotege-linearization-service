package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.EventProcesableParameter;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;


public class LinearizationSpecification extends EventProcesableParameter {

    private final ThreeStateBoolean isAuxiliaryAxisChild;

    private final ThreeStateBoolean isGrouping;

    private final ThreeStateBoolean isIncludedInLinearization;

    private final IRI linearizationParent;

    private final IRI linearizationView;

    private final String codingNote;

    @JsonCreator
    public LinearizationSpecification(@JsonProperty("isAuxiliaryAxisChild") ThreeStateBoolean isAuxiliaryAxisChild,
                                      @JsonProperty("isGrouping") ThreeStateBoolean isGrouping,
                                      @JsonProperty("isIncludedInLinearization") ThreeStateBoolean isIncludedInLinearization,
                                      @JsonProperty("linearizationParent") IRI linearizationParent,
                                      @JsonProperty("linearizationView") @Nonnull IRI linearizationView,
                                      @JsonProperty("codingNote") String codingNote) {
        this.isAuxiliaryAxisChild = isAuxiliaryAxisChild;
        this.isGrouping = isGrouping;
        this.isIncludedInLinearization = isIncludedInLinearization;
        this.linearizationParent = linearizationParent;
        this.linearizationView = checkNotNull(linearizationView);
        this.codingNote = codingNote;
    }


    public ThreeStateBoolean getIsAuxiliaryAxisChild() {
        return isAuxiliaryAxisChild;
    }

    public ThreeStateBoolean getIsGrouping() {
        return isGrouping;
    }

    public ThreeStateBoolean getIsIncludedInLinearization() {
        return isIncludedInLinearization;
    }

    public IRI getLinearizationParent() {
        return linearizationParent;
    }

    public IRI getLinearizationView() {
        return linearizationView;
    }

    public String getCodingNote() {
        return codingNote;
    }
}
