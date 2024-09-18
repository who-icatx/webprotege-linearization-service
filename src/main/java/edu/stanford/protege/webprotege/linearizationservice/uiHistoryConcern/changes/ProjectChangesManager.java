package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import com.google.common.collect.ImmutableList;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.diff.DiffElement;
import edu.stanford.protege.webprotege.entity.EntityNode;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationHistoryService;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.diff.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.nodeRendering.*;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import org.semanticweb.owlapi.model.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static edu.stanford.protege.webprotege.linearizationservice.mappers.LinearizationEventMapper.groupEventsByViews;


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
            ImmutableList<ProjectChange> projectChanges = getChangesForEntity(subject.getIRI(), projectId, pageRequest, linearizationDefinitions);
            return Page.create(1, 1, projectChanges, projectChanges.size());
        } else {
            // Pages are in reverse order
            ImmutableList<ProjectChange> projectChanges = getPaginatedChangesForProject(projectId, pageRequest, linearizationDefinitions);

            int pageCount = (projectChanges.size() / pageRequest.getPageSize()) + 1;
            if (pageRequest.getPageNumber() > pageCount) {
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

        GetRenderedOwlEntitiesResult renderedEntities = null;
        try {
            renderedEntities = entityRendererManager.getRenderedEntities(Set.of(iri.toString()), projectId, new ExecutionContext()).get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error(e.getMessage());
        }

        // We need to scan revisions to find the ones containing a particular subject
        // We ignore the page request here.
        // This needs reworking really, but the number of changes per entity is usually small
        // so this works for now.
        ImmutableList.Builder<ProjectChange> changes = ImmutableList.builder();
        List<EntityNode> finalRenderedEntities = renderedEntities != null ? renderedEntities.renderedEntities() : List.of();
        revisions.stream()
                .skip(pageRequest.getSkip())
                .limit(pageRequest.getPageSize())
                .forEach(revision -> getProjectChangesForRevision(revision, iri.toString(), finalRenderedEntities, changes, linearizationDefinitions));

        return changes.build();
    }

    private ImmutableList<ProjectChange> getPaginatedChangesForProject(ProjectId projectId, PageRequest pageRequest, List<LinearizationDefinition> linearizationDefinitions) {
        ImmutableList.Builder<ProjectChange> changes = ImmutableList.builder();
        List<LinearizationRevisionWithEntity> paginatedHistory = historyService.getAllExistingHistoriesForProjectWithPageAndPageSize(projectId, pageRequest.getPageNumber(), pageRequest.getPageSize());

        List<String> entityIrisPaginated = new ArrayList<>();
        paginatedHistory.forEach(linRevisionWithEntity -> entityIrisPaginated.add(linRevisionWithEntity.getWhoficEntityIri()));

        //Make request just for the paginated entity changes
        GetRenderedOwlEntitiesResult renderedEntities = null;
        try {
            renderedEntities = entityRendererManager.getRenderedEntities(new HashSet<>(entityIrisPaginated), projectId, new ExecutionContext()).get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error(e.getMessage());
        }
        List<EntityNode> renderedEntitiesList = renderedEntities != null ? renderedEntities.renderedEntities() : List.of();

        //Here we add the rendered entity name for the view
        paginatedHistory.stream().flatMap(revisionWithEntity -> {
            var entityTextOptional = renderedEntitiesList
                    .stream()
                    .filter(entityNode -> entityNode.getEntity().getIRI().toString().equals(revisionWithEntity.getWhoficEntityIri()))
                    .map(EntityNode::getBrowserText)
                    .findFirst();
            if (entityTextOptional.isEmpty()) {
                return Stream.of(revisionWithEntity);
            }
            return Stream.of(new LinearizationRevisionWithEntity(revisionWithEntity.getRevision(), entityTextOptional.get()));
        }).forEach(revisionWithEntity ->
                getProjectChangesForRevision(
                        revisionWithEntity.getRevision(),
                        revisionWithEntity.getWhoficEntityIri(),
                        renderedEntitiesList,
                        changes,
                        linearizationDefinitions
                )
        );

        return changes.build();
    }

    private void getProjectChangesForRevision(LinearizationRevision revision,
                                              String subjectName,
                                              List<EntityNode> renderedEntities,
                                              ImmutableList.Builder<ProjectChange> changesBuilder,
                                              List<LinearizationDefinition> linearizationDefinitions) {
        final int totalChanges;
        var changesByView = groupEventsByViews(revision.linearizationEvents().stream().toList());
        totalChanges = changesByView.size();

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = revision2DiffElementsTranslator.getDiffElementsFromRevision(changesByView, linearizationDefinitions);
        diffElements.sort(
                Comparator.comparing(diffElement -> diffElement.getSourceDocument().getSortingCode())
        );
        List<DiffElement<String, String>> renderedDiffElements = renderDiffElements(diffElements, renderedEntities);
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

    public ProjectChange getProjectChangesForRevision(LinearizationRevision revision,
                                                      String subjectName,
                                                      List<EntityNode> renderedEntities,
                                                      List<LinearizationDefinition> linearizationDefinitions) {
        final int totalChanges;
        var changesByView = groupEventsByViews(revision.linearizationEvents().stream().toList());
        totalChanges = changesByView.size();

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = revision2DiffElementsTranslator.getDiffElementsFromRevision(changesByView, linearizationDefinitions);
        diffElements.sort(
                Comparator.comparing(diffElement -> diffElement.getSourceDocument().getSortingCode())
        );
        List<DiffElement<String, String>> renderedDiffElements = renderDiffElements(diffElements, renderedEntities);
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
        return projectChange;
    }

    private List<DiffElement<String, String>> renderDiffElements(List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements, List<EntityNode> renderedEntities) {

        List<DiffElement<String, String>> renderedDiffElements = new ArrayList<>();
        DiffElementRenderer<String> renderer = new DiffElementRenderer<>(renderedEntities);
        for (DiffElement<LinearizationDocumentChange, LinearizationEventsForView> diffElement : diffElements) {
            renderedDiffElements.add(renderer.render(diffElement));
        }
        return renderedDiffElements;
    }

    public List<ProjectChangeForEntity> getProjectChangesForHistories(ProjectId projectId, List<EntityLinearizationHistory> entityLinearizationHistories) {
        Map<String, String> entityIrisAndNames = new HashMap<>();
        List<LinearizationRevisionWithEntity> linRevisions = entityLinearizationHistories.stream()
                .flatMap(history ->
                        history.getLinearizationRevisions()
                                .stream()
                                .map(revision -> new LinearizationRevisionWithEntity(revision, history.getWhoficEntityIri()))
                )
                .sorted(Comparator.comparing(LinearizationRevisionWithEntity::getRevision).reversed())
                .peek(revisionWithEntity -> entityIrisAndNames.put(revisionWithEntity.getWhoficEntityIri(), revisionWithEntity.getWhoficEntityIri()))
                .toList();

        List<EntityNode> renderedEntitiesList = entityRendererManager.getRenderedEntities(entityIrisAndNames.keySet(), projectId);
        var linearizationDefinitions = definitionRepository.getLinearizationDefinitions();

        linRevisions.forEach(revisionWithEntity -> {
            var entityTextOptional = renderedEntitiesList
                    .stream()
                    .filter(entityNode -> entityNode.getEntity().getIRI().toString().equals(revisionWithEntity.getWhoficEntityIri()))
                    .map(EntityNode::getBrowserText)
                    .findFirst();
            entityTextOptional.ifPresent(s -> entityIrisAndNames.put(revisionWithEntity.getWhoficEntityIri(), s));
        });

        List<ProjectChangeForEntity> projectChangeForEntityList = linRevisions.stream()
                .flatMap(revisionWithEntity -> {
                            ProjectChange projectChange = getProjectChangesForRevision(
                                    revisionWithEntity.getRevision(),
                                    entityIrisAndNames.get(revisionWithEntity.getWhoficEntityIri()),
                                    renderedEntitiesList,
                                    linearizationDefinitions
                            );
                            ProjectChangeForEntity projectChangeForEntity = ProjectChangeForEntity.create(
                                    revisionWithEntity.getWhoficEntityIri(),
                                    projectChange
                            );
                            return Stream.of(projectChangeForEntity);
                        }
                )
                .toList();

        return projectChangeForEntityList;
    }
}
