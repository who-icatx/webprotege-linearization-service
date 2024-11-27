package edu.stanford.protege.webprotege.linearizationservice.services;


import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.model.CommitStatus;
import edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationRevision;
import edu.stanford.protege.webprotege.linearizationservice.repositories.history.LinearizationHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RevisionCommitService {


    private final LinearizationHistoryRepository repository;

    public RevisionCommitService(LinearizationHistoryRepository repository) {
        this.repository = repository;
    }


    public void rollbackRevision(ChangeRequestId changeRequestId, ProjectId projectId, String entityIri) {
        repository.deleteRevision(changeRequestId, projectId, entityIri);
    }


    public void commitRevision(ChangeRequestId changeRequestId, ProjectId projectId, String entityIri) {
        repository.commitRevision(changeRequestId,projectId, entityIri);
    }


}
