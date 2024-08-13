package edu.stanford.protege.webprotege.linearizationservice.testUtils;

import edu.stanford.protege.webprotege.linearizationservice.model.ThreeStateBoolean;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationViewIriHelper.getLinearizationViewIris;

public class RandomHelper {
    private static final Random RANDOM = new Random();

    private static final List<ThreeStateBoolean> SAMPLE_BOOLS = List.of(ThreeStateBoolean.TRUE, ThreeStateBoolean.FALSE, ThreeStateBoolean.UNKNOWN);

    public static String getRandomIri() {
        return "http://id.who.int/icd/entity/" + UUID.randomUUID();
    }

    public static IRI getRandomLinearizationView() {
        var iriList = getLinearizationViewIris();
        int randomIndex = RANDOM.nextInt(iriList.size());
        return iriList.get(randomIndex);
    }

    public static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    public static ThreeStateBoolean getRandomThreeStateBoolean() {
        return SAMPLE_BOOLS.get(RANDOM.nextInt(SAMPLE_BOOLS.size()));
    }
}
