package edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes;

import com.google.common.collect.ImmutableList;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.entity.EntityNode;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.LinearizationHistoryService;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.diff.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.nodeRendering.*;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.renderer.GetEntityHtmlRenderingResult;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import org.semanticweb.owlapi.model.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.mappers.LinearizationEventMapper.groupEventsByViews;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27/05/15
 */
@Component
public class ProjectChangesManager {

    private static final Logger logger = LoggerFactory.getLogger(ProjectChangesManager.class);

    private final Revision2DiffElementsTranslator revision2DiffElementsTranslator;

    private final LinearizationHistoryService historyService;

    private final LinearizationDefinitionRepository definitionRepository;

    private final EntityRendererManager entityRendererManager;

    public ProjectChangesManager(
            Revision2DiffElementsTranslator revision2DiffElementsTranslator,
            LinearizationHistoryService historyService,
            LinearizationDefinitionRepository definitionRepository,
            EntityRendererManager entityRendererManager) {
        this.revision2DiffElementsTranslator = revision2DiffElementsTranslator;
        this.historyService = historyService;
        this.definitionRepository = definitionRepository;
        this.entityRendererManager = entityRendererManager;
    }

    public Page<ProjectChange> getProjectChanges(OWLEntity subject,
                                                 ProjectId projectId,
                                                 PageRequest pageRequest) {

        var linearizationDefinitions = definitionRepository.getLinearizationDefinitions();
        if (subject != null) {
            ImmutableList<ProjectChange> theChanges = getChangesForEntity(subject.getIRI(), projectId, pageRequest, linearizationDefinitions);
            return Page.create(1, 1, theChanges, theChanges.size());
        } else {
            // Pages are in reverse order
            ImmutableList<ProjectChange> projectChanges = getChangesForFullProject(projectId, pageRequest, linearizationDefinitions);

            int pageCount = (projectChanges.size() / pageRequest.getPageSize()) + 1;
            if(pageRequest.getPageNumber()>pageCount){
                return Page.emptyPage();
            }
            return Page.create(pageRequest.getPageNumber(),
                    pageCount,
                    projectChanges,
                    projectChanges.size());
        }
    }

    private ImmutableList<ProjectChange> getChangesForEntity(IRI iri, ProjectId projectId, PageRequest pageRequest, List<LinearizationDefinition> linearizationDefinitions) {
        List<LinearizationRevision> revisions = new ArrayList<>();
        Optional<EntityLinearizationHistory> optionalHistory = historyService.getExistingHistoryOrderedByRevision(iri, projectId);
        optionalHistory.ifPresent(history -> revisions.addAll(history.getLinearizationRevisions()));

        GetEntityHtmlRenderingResult renderedEntity = null;
        try {
            renderedEntity = entityRendererManager.getRenderedEntity(iri, projectId, new ExecutionContext()).get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error(e.getMessage());
        }
        String entityName = iri.toString();
        if (renderedEntity != null &&
                !renderedEntity.rendering().trim().equals("")) {
            entityName = renderedEntity.rendering();
        }

        final var finalEntityName = entityName;
        // We need to scan revisions to find the ones containing a particular subject
        // We ignore the page request here.
        // This needs reworking really, but the number of changes per entity is usually small
        // so this works for now.
        ImmutableList.Builder<ProjectChange> changes = ImmutableList.builder();
        revisions.stream()
                .skip(pageRequest.getSkip())
                .limit(pageRequest.getPageSize())
                .forEach(revision -> getProjectChangesForRevision(revision, finalEntityName, changes, linearizationDefinitions));

        return changes.build();
    }

    private ImmutableList<ProjectChange> getChangesForFullProject(ProjectId projectId, PageRequest pageRequest, List<LinearizationDefinition> linearizationDefinitions) {
        ImmutableList.Builder<ProjectChange> changes = ImmutableList.builder();
        List<EntityLinearizationHistory> fullHistory = historyService.getAllExistingHistoriesForProject(projectId);

        var entityIris = fullHistory.stream().flatMap(history -> Stream.of(history.getWhoficEntityIri())).collect(Collectors.toSet());
        GetRenderedOwlEntitiesResult renderedEntities = null;
        try {
            renderedEntities = entityRendererManager.getRenderedEntities(entityIris, projectId, new ExecutionContext()).get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error(e.getMessage());
        }
        final var renderedEntitiesResult = renderedEntities;
        fullHistory.stream()
                .flatMap(history ->
                        history.getLinearizationRevisions()
                                .stream()
                                .map(revision -> {
                                            if (renderedEntitiesResult == null || renderedEntitiesResult.renderedEntities() == null) {
                                                return new LinearizationRevisionWithEntity(revision, history.getWhoficEntityIri());
                                            }
                                            var entityTextOptional = renderedEntitiesResult.renderedEntities()
                                                    .stream()
                                                    .filter(entityNode -> entityNode.getEntity().getIRI().toString().equals(history.getWhoficEntityIri()))
                                                    .map(EntityNode::getBrowserText)
                                                    .findFirst();
                                            if (entityTextOptional.isEmpty()) {
                                                return new LinearizationRevisionWithEntity(revision, history.getWhoficEntityIri());
                                            }
                                            return new LinearizationRevisionWithEntity(revision, entityTextOptional.get());
                                        }
                                )
                )
                .sorted(Comparator.comparing(LinearizationRevisionWithEntity::getRevision).reversed())
                .skip(pageRequest.getSkip())
                .limit(pageRequest.getPageSize())
                .forEach(revisionWithEntity -> getProjectChangesForRevision(revisionWithEntity.getRevision(), revisionWithEntity.getWhoficEntityName(), changes, linearizationDefinitions));

        return changes.build();
    }

    public ImmutableList<ProjectChange> getProjectChangesForSubjectInRevision(OWLEntity subject, LinearizationRevision revision, List<LinearizationDefinition> linearizationDefinitions) {
        ImmutableList.Builder<ProjectChange> resultBuilder = ImmutableList.builder();
        getProjectChangesForRevision(revision, subject.getIRI().toString(), resultBuilder, linearizationDefinitions);
        return resultBuilder.build();
    }

    private void getProjectChangesForRevision(LinearizationRevision revision,
                                              String subjectName,
                                              ImmutableList.Builder<ProjectChange> changesBuilder,
                                              List<LinearizationDefinition> linearizationDefinitions) {
        final int totalChanges;
        var changesByView = groupEventsByViews(revision.linearizationEvents().stream().toList());
        totalChanges = changesByView.size();

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = revision2DiffElementsTranslator.getDiffElementsFromRevision(changesByView, linearizationDefinitions);
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
                RevisionNumber.valueOf("0"),
                revision.userId(),
                revision.timestamp(),
                "Edited Linearization for Entity: " + subjectName,
                totalChanges,
                page);
        changesBuilder.add(projectChange);
    }

    private List<DiffElement<String, String>> renderDiffElements(List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements) {

        List<DiffElement<String, String>> renderedDiffElements = new ArrayList<>();
        DiffElementRenderer<String> renderer = new DiffElementRenderer<>();
        for (DiffElement<LinearizationDocumentChange, LinearizationEventsForView> diffElement : diffElements) {
            renderedDiffElements.add(renderer.render(diffElement));
        }
        return renderedDiffElements;
    }
}
