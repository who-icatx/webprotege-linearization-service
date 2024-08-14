package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

public class LinearizationDocumentChange {
    private final String linearizationViewIri;
    private final String linearizationViewName;
    private final String linearizationViewId;
    private final String sortingCode;

    private LinearizationDocumentChange(String linearizationViewIri,
                                        String linearizationViewName,
                                        String linearizationViewId,
                                        String sortingCode) {
        this.linearizationViewIri = linearizationViewIri;
        this.linearizationViewName = linearizationViewName;
        this.linearizationViewId = linearizationViewId;
        this.sortingCode = sortingCode;
    }

    public static LinearizationDocumentChange create(String linearizationViewIri,
                                                     String linearizationViewName,
                                                     String linearizationViewId,
                                                     String sortingCode) {
        return new LinearizationDocumentChange(linearizationViewIri, linearizationViewName, linearizationViewId, sortingCode);
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

    public String getSortingCode() {
        return sortingCode;
    }
}
