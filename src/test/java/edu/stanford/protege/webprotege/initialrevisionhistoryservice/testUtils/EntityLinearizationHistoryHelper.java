package edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.LinearizationRevisionHelper.getLinearizationRevisions;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.getRandomIri;

public class EntityLinearizationHistoryHelper {

    public static EntityLinearizationHistory getEntityLinearizationHistory(ProjectId projectId, int numberOfRevisions) {

        return new EntityLinearizationHistory(getRandomIri(), projectId.id(), getLinearizationRevisions(numberOfRevisions));
    }

    public static EntityLinearizationHistory getEntityLinearizationHistoryForEntityIri(String entityIRI, ProjectId projectId, int numberOfRevisions) {

        return new EntityLinearizationHistory(entityIRI, projectId.id(), getLinearizationRevisions(numberOfRevisions));
    }
}
