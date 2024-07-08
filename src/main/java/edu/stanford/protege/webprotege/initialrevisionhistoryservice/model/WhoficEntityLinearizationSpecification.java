package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.IRI;

import java.util.List;

public record WhoficEntityLinearizationSpecification(@JsonProperty("whoficEntityIri") IRI entityIRI,
                                                     @JsonProperty("linearizationSpecifications") List<LinearizationSpecification> linearizationSpecifications) {

}
