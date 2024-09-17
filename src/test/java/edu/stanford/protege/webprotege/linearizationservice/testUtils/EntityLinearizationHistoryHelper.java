package edu.stanford.protege.webprotege.linearizationservice.testUtils;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationRevisionHelper.getLinearizationRevisions;
import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.getRandomIri;

public class EntityLinearizationHistoryHelper {

    public static EntityLinearizationHistory getEntityLinearizationHistory(ProjectId projectId, int numberOfRevisions) {

        return new EntityLinearizationHistory(getRandomIri(), projectId.id(), getLinearizationRevisions(numberOfRevisions));
    }

    public static EntityLinearizationHistory getEntityLinearizationHistory(String entityIriProjectId, ProjectId projectId, int numberOfRevisions) {

        return new EntityLinearizationHistory(entityIriProjectId, projectId.id(), getLinearizationRevisions(numberOfRevisions));
    }

    public static EntityLinearizationHistory getEntityLinearizationHistoryForEntityIri(String entityIRI, ProjectId projectId, int numberOfRevisions) {

        return new EntityLinearizationHistory(entityIRI, projectId.id(), getLinearizationRevisions(numberOfRevisions));
    }
}
