package edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils;

import org.semanticweb.owlapi.model.IRI;

import java.util.List;

public class LinearizationViewIriHelper {
    private static final IRI linearizationView1 = IRI.create("http://id.who.int/icd/entity/MMS");
    private static final IRI linearizationView2 = IRI.create("http://id.who.int/icd/entity/primCareLowResSet");
    private static final IRI linearizationView3 = IRI.create("http://id.who.int/icd/entity/research");
    private static final IRI linearizationView4 = IRI.create("http://id.who.int/icd/entity/mentalHealth");
    private static final IRI linearizationView5 = IRI.create("http://id.who.int/icd/entity/dermatology");
    private static final IRI linearizationView6 = IRI.create("http://id.who.int/icd/entity/musculoskeletal");
    private static final IRI linearizationView7 = IRI.create("http://id.who.int/icd/entity/neurology");
    private static final IRI linearizationView8 = IRI.create("http://id.who.int/icd/entity/paediatrics");
    private static final IRI linearizationView9 = IRI.create("http://id.who.int/icd/entity/ocupHealth");
    private static final IRI linearizationView10 = IRI.create("http://id.who.int/icd/entity/envHealth");
    private static final IRI linearizationView11 = IRI.create("http://id.who.int/icd/entity/rareDiseases");
    private static final IRI linearizationView12 = IRI.create("http://id.who.int/icd/entity/ophthalmology");
    private static final IRI linearizationView13 = IRI.create("http://id.who.int/icd/entity/ICD-O");
    private static final IRI linearizationView14 = IRI.create("http://id.who.int/icd/entity/mortality");
    private static final IRI linearizationView15 = IRI.create("http://id.who.int/icd/entity/SMoL");
    private static final IRI linearizationView16 = IRI.create("http://id.who.int/icd/entity/primaryCareHighResSet");

    private static final List<IRI> IRIS = List.of(
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

    public static List<IRI> getLinearizationViewIris() {
        return IRIS;
    }
}
