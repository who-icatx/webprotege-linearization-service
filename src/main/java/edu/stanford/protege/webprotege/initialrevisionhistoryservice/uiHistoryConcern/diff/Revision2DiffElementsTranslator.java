package edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.diff;


import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/15
 */
@Component
public class Revision2DiffElementsTranslator {

    private final OntologyIRIShortFormProvider ontologyIRIShortFormProvider = new OntologyIRIShortFormProvider();


    public Revision2DiffElementsTranslator() {
    }

    public List<DiffElement<String, LinearizationEvent>> getDiffElementsFromRevision(List<LinearizationEvent> revision, IRI entityIri) {
        final List<DiffElement<String, LinearizationEvent>> changeRecordElements = new ArrayList<>();
        for (final LinearizationEvent event : revision) {
            changeRecordElements.add(toElement(event, entityIri));
        }
        return changeRecordElements;
    }

    private DiffElement<String, LinearizationEvent> toElement(LinearizationEvent linearizationEvent, IRI entityIri) {
        final String ontologyIRIShortForm;

        ontologyIRIShortForm = ontologyIRIShortFormProvider.getShortForm(entityIri);


        return new DiffElement<>(
                getDiffOperation(),
                ontologyIRIShortForm,
                linearizationEvent);
    }

    private DiffOperation getDiffOperation() {
        return DiffOperation.ADD;
    }
}
