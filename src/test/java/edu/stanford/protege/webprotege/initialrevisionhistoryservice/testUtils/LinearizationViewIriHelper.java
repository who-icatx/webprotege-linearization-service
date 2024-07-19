package edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils;

import org.semanticweb.owlapi.model.IRI;

import java.util.List;

public class LinearizationViewIriHelper {
    private static final String linearizationView1 = "http://id.who.int/icd/entity/MMS";
    private static final String linearizationView2 = "http://id.who.int/icd/entity/primCareLowResSet";
    private static final String linearizationView3 = "http://id.who.int/icd/entity/research";
    private static final String linearizationView4 = "http://id.who.int/icd/entity/mentalHealth";
    private static final String linearizationView5 = "http://id.who.int/icd/entity/dermatology";
    private static final String linearizationView6 = "http://id.who.int/icd/entity/musculoskeletal";
    private static final String linearizationView7 = "http://id.who.int/icd/entity/neurology";
    private static final String linearizationView8 = "http://id.who.int/icd/entity/paediatrics";
    private static final String linearizationView9 = "http://id.who.int/icd/entity/ocupHealth";
    private static final String linearizationView10 = "http://id.who.int/icd/entity/envHealth";
    private static final String linearizationView11 = "http://id.who.int/icd/entity/rareDiseases";
    private static final String linearizationView12 = "http://id.who.int/icd/entity/ophthalmology";
    private static final String linearizationView13 = "http://id.who.int/icd/entity/ICD-O";
    private static final String linearizationView14 = "http://id.who.int/icd/entity/mortality";
    private static final String linearizationView15 = "http://id.who.int/icd/entity/SMoL";
    private static final String linearizationView16 = "http://id.who.int/icd/entity/primaryCareHighResSet";

    private static final List<String> IRIS = List.of(
            linearizationView1,
            linearizationView2,
            linearizationView3,
            linearizationView4,
            linearizationView5,
            linearizationView6,
            linearizationView7,
            linearizationView8,
            linearizationView9,
            linearizationView10,
            linearizationView11,
            linearizationView12,
            linearizationView13,
            linearizationView14,
            linearizationView15,
            linearizationView16
    );

    public static List<String> getLinearizationViewIris() {
        return IRIS;
    }
}
