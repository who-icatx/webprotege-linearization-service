package edu.stanford.protege.webprotege.linearizationservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.linearizationservice.events.EventProcesableParameter;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;


public class LinearizationSpecification extends EventProcesableParameter {

    private final LinearizationStateCell isAuxiliaryAxisChild;

    private final LinearizationStateCell isGrouping;

    private final LinearizationStateCell isIncludedInLinearization;

    private final IRI linearizationParent;

    private final IRI linearizationView;

    private final String codingNote;

    @JsonCreator
    public LinearizationSpecification(@JsonProperty("isAuxiliaryAxisChild") LinearizationStateCell isAuxiliaryAxisChild,
                                      @JsonProperty("isGrouping") LinearizationStateCell isGrouping,
                                      @JsonProperty("isIncludedInLinearization") LinearizationStateCell isIncludedInLinearization,
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


    public LinearizationStateCell getIsAuxiliaryAxisChild() {
        return isAuxiliaryAxisChild;
    }

    public LinearizationStateCell getIsGrouping() {
        return isGrouping;
    }

    public LinearizationStateCell getIsIncludedInLinearization() {
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
