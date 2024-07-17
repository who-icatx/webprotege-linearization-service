package edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.LinearizationViewIriHelper.getLinearizationViewIris;

public class RandomHelper {
    private static final Random RANDOM = new Random();

    private static final List<String> SAMPLE_STRINGS = List.of("string1", "string2", "string3", "string4", "string5", "string6");

    private static final List<ThreeStateBoolean> SAMPLE_BOOLS = List.of(ThreeStateBoolean.TRUE, ThreeStateBoolean.FALSE, ThreeStateBoolean.UNKNOWN);

    private static final List<String> SAMPLE_IRIS = List.of(
            "http://id.who.int/icd/entity/iri1",
            "http://id.who.int/icd/entity/iri2",
            "http://id.who.int/icd/entity/iri3",
            "http://id.who.int/icd/entity/iri4",
            "http://id.who.int/icd/entity/iri5",
            "http://id.who.int/icd/entity/iri6",
            "http://id.who.int/icd/entity/iri7",
            "http://id.who.int/icd/entity/iri8",
            "http://id.who.int/icd/entity/iri9",
            "http://id.who.int/icd/entity/iri10"
    );

    public static String getRandomIri() {
        return SAMPLE_IRIS.get(RANDOM.nextInt(SAMPLE_IRIS.size()));
    }

    public static IRI getRandomLinearizationView() {
        var iriList = getLinearizationViewIris();
        int randomIndex = RANDOM.nextInt(iriList.size());
        return iriList.get(randomIndex);
    }

    public static String getRandomString() {
        return SAMPLE_STRINGS.get(RANDOM.nextInt(SAMPLE_STRINGS.size()));
    }

    public static ThreeStateBoolean getRandomThreeStateBoolean() {
        return SAMPLE_BOOLS.get(RANDOM.nextInt(SAMPLE_BOOLS.size()));
    }
}
