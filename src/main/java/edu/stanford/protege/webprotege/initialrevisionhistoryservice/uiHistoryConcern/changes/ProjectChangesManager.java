package edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes;

import com.google.common.collect.ImmutableList;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.LinearizationHistoryService;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.diff.*;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import org.semanticweb.owlapi.model.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27/05/15
 */
@Component
public class ProjectChangesManager {

    private static final Logger logger = LoggerFactory.getLogger(ProjectChangesManager.class);

    public static final int DEFAULT_CHANGE_LIMIT = 50;

    private final Revision2DiffElementsTranslator revision2DiffElementsTranslator;

    private final LinearizationHistoryService historyService;

    public ProjectChangesManager(
            Revision2DiffElementsTranslator revision2DiffElementsTranslator,
            LinearizationHistoryService historyService) {
        this.revision2DiffElementsTranslator = revision2DiffElementsTranslator;
        this.historyService = historyService;
    }

    public Page<ProjectChange> getProjectChanges(OWLEntity subject,
                                                 ProjectId projectId,
                                                 PageRequest pageRequest) {
        if (subject != null) {
            ImmutableList<ProjectChange> theChanges = getChangesForEntity(subject.getIRI(), projectId, pageRequest);
            return Page.create(1, 1, theChanges, theChanges.size());
        } else {
            // Pages are in reverse order
            ImmutableList<ProjectChange> projectChanges = getChangesForFullProject(projectId, pageRequest);

            int pageCount = (projectChanges.size() / pageRequest.getPageSize()) + 1;
            return Page.create(pageRequest.getPageNumber(),
                    pageCount,
                    projectChanges.reverse(), projectChanges.size());
        }
    }

    private ImmutableList<ProjectChange> getChangesForEntity(IRI iri, ProjectId projectId, PageRequest pageRequest) {
        List<LinearizationRevision> revisions = new ArrayList<>();
        Optional<EntityLinearizationHistory> optionalHistory = historyService.getExistingHistoryOrderedByRevision(iri, projectId);
        optionalHistory.ifPresent(history -> revisions.addAll(history.getLinearizationRevisions()));
        // We need to scan revisions to find the ones containing a particular subject
        // We ignore the page request here.
        // This needs reworking really, but the number of changes per entity is usually small
        // so this works for now.
        ImmutableList.Builder<ProjectChange> changes = ImmutableList.builder();
        revisions.stream()
                .skip(pageRequest.getSkip())
                .limit(pageRequest.getPageSize())
                .forEach(revision -> getProjectChangesForRevision(revision, iri, changes));

        return changes.build();
    }

    private ImmutableList<ProjectChange> getChangesForFullProject(ProjectId projectId, PageRequest pageRequest) {
        ImmutableList.Builder<ProjectChange> changes = ImmutableList.builder();
        List<EntityLinearizationHistory> fullHistory = historyService.getAllExistingHistoriesForProject(projectId);

        fullHistory.stream()
                .skip(pageRequest.getSkip())
                .limit(pageRequest.getPageSize())
                .flatMap(history ->
                        history.getLinearizationRevisions()
                                .stream()
                                .map(revision -> new LinearizationRevisionWithEntity(revision, IRI.create(history.getWhoficEntityIri())))
                )
                .sorted(Comparator.comparing(LinearizationRevisionWithEntity::getRevision))
                .forEach(revisionWithEntity -> getProjectChangesForRevision(revisionWithEntity.getRevision(), revisionWithEntity.getWhoficEntityIri(), changes));

        return changes.build();
    }

    public ImmutableList<ProjectChange> getProjectChangesForSubjectInRevision(OWLEntity subject, LinearizationRevision revision) {
        ImmutableList.Builder<ProjectChange> resultBuilder = ImmutableList.builder();
        getProjectChangesForRevision(revision, subject.getIRI(), resultBuilder);
        return resultBuilder.build();
    }

    private void getProjectChangesForRevision(LinearizationRevision revision,
                                              IRI subject,
                                              ImmutableList.Builder<ProjectChange> changesBuilder) {

        List<LinearizationEvent> limitedRecords = new ArrayList<>();
        final int totalChanges;

        totalChanges = revision.linearizationEvents().size();
        revision.linearizationEvents().stream()
                .limit(DEFAULT_CHANGE_LIMIT)
                .forEach(limitedRecords::add);


        List<DiffElement<String, LinearizationEventsForView>> diffElements = revision2DiffElementsTranslator.getDiffElementsFromRevision(limitedRecords, subject);
        List<DiffElement<String, String>> renderedDiffElements = renderDiffElements(diffElements);
        int pageElements = renderedDiffElements.size();
        int pageCount;
        if (pageElements == 0) {
            pageCount = 1;
        } else {
            pageCount = totalChanges / pageElements + (totalChanges % pageElements);
        }
        Page<DiffElement<String, String>> page = Page.create(
                1,
                pageCount,
                renderedDiffElements,
                totalChanges
        );
        ProjectChange projectChange = ProjectChange.get(
                RevisionNumber.valueOf("1"),
                revision.userId(),
                revision.timestamp(),
                "Edited Linearization for Entity: " + subject.toString(),
                totalChanges,
                page);
        changesBuilder.add(projectChange);
    }

    private List<DiffElement<String, String>> renderDiffElements(List<DiffElement<String, LinearizationEventsForView>> diffElements) {

        List<DiffElement<String, String>> renderedDiffElements = new ArrayList<>();
        DiffElementRenderer<String> renderer = new DiffElementRenderer<>();
        for (DiffElement<String, LinearizationEventsForView> diffElement : diffElements) {
            renderedDiffElements.add(renderer.render(diffElement));
        }
        return renderedDiffElements;
    }
}
