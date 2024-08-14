package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

public class LinearizationDocumentChange {
    private final String linearizationViewIri;
    private final String linearizationViewName;
    private final String linearizationViewId;

    private LinearizationDocumentChange(String linearizationViewIri,
                                        String linearizationViewName, String linearizationViewId) {
        this.linearizationViewIri = linearizationViewIri;
        this.linearizationViewName = linearizationViewName;
        this.linearizationViewId = linearizationViewId;
    }

    public static LinearizationDocumentChange create(String linearizationViewIri,
                                                     String linearizationViewName,
                                                     String linearizationViewId) {
        return new LinearizationDocumentChange(linearizationViewIri, linearizationViewName, linearizationViewId);
    }

    public String getLinearizationViewIri() {
        return linearizationViewIri;
    }

    public String getLinearizationViewName() {
        return linearizationViewName;
    }

    public String getLinearizationViewId() {
        return linearizationViewId;
    }
}
