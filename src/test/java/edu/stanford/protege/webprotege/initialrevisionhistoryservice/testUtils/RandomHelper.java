package edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils;

import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.LinearizationViewIriHelper.getLinearizationViewIris;

public class RandomHelper {
    private static final Random RANDOM = new Random();

    private static final List<IRI> SAMPLE_IRIS = List.of(
            IRI.create("http://id.who.int/icd/entity/iri1"),
            IRI.create("http://id.who.int/icd/entity/iri2"),
            IRI.create("http://id.who.int/icd/entity/iri3")
    );

    public static IRI getRandomIri() {
        return SAMPLE_IRIS.get(RANDOM.nextInt(SAMPLE_IRIS.size()));
    }

    public static IRI getRandomLinearizationView() {
        var iriList = getLinearizationViewIris();
        int randomIndex = RANDOM.nextInt(iriList.size());
        return iriList.get(randomIndex);
    }

}
